# PagingExample
I had a chance to explore the Paging library recently that is part of the Android Jetpack.
So I thought I would write about the 7 basic steps to implement the Paging library in an Android app.

So let’s begin!
# Step 1: Add the Paging library to the app
    /* 
     * This is just to enable Java 8 in the app
     */
      compileOptions {
          sourceCompatibility JavaVersion.VERSION_1_8
          targetCompatibility JavaVersion.VERSION_1_8
      }
    /* 
     * I prefer using data binding so we need to enable it first
     */
    android {
        dataBinding {
            enabled = true
        }
    }
    /* 
     * Step 1: Add the paging library
     */
    implementation 'android.arch.paging:runtime:1.0.0'
    
    /* 
     * Step 2: Adding ViewModel and Lifecycle  
     */
    implementation 'android.arch.lifecycle:extensions:1.1.1'


    /* 
     * Step 3: Adding rxJava to the app  
     */
    implementation 'io.reactivex.rxjava2:rxjava:2.1.9'
    implementation 'io.reactivex.rxjava2:rxandroid:2.0.2'

    /* 
     * Step 4: Adding retrofit to the app  
     */
    implementation 'com.squareup.retrofit2:retrofit:2.4.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.4.0'
    implementation 'com.squareup.retrofit2:adapter-rxjava2:2.4.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:3.9.1'


    /* 
     * Step 5: We would be needing an image loading library.
     * So we are going to use Picasso
     */
    implementation 'com.squareup.picasso:picasso:2.71828'
# Step 2: Setup retrofit to fetch news feed
    interface RestApi {
      //https://newsapi.org/v2/everything?q=movies&apiKey=079dac74a5f94ebdb990ecf61c8854b7&pageSize=20&page=2
      @GET("/v2/everything")
      fun fetchFeed(
          @Query("q") q: String?,
          @Query("apiKey") apiKey: String?,
          @Query("page") page: Long,
          @Query("pageSize") pageSize: Int
      ): Call<Feed>
    }
And a service class: RestApiFactory.java

    object RestApiFactory {
        private const val BASE_URL = "https://newsapi.org"

        fun create(): RestApi {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)
            val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(httpClient.build())
                .build()
            return retrofit.create(RestApi::class.java)
        }
    }

# Step 3: Setup the dataSource

    class FeedDataSource : PageKeyedDataSource<Long, Article>() {

        companion object {
            private const val TAG = "FeedDataSource"
        }

        val networkState: MutableLiveData<NetworkState> by lazy {
            MutableLiveData<NetworkState>()
        }


        override fun loadInitial(
            params: LoadInitialParams<Long>,
            callback: LoadInitialCallback<Long, Article>
        ) {
            networkState.postValue(NetworkState.LOADING)

            AppController.restApi.fetchFeed(QUERY, API_KEY, 1, params.requestedLoadSize)
                .enqueue(object : retrofit2.Callback<Feed> {
                    override fun onFailure(call: Call<Feed>, t: Throwable) {
                        if(t.message != null) {
                            networkState.postValue(NetworkState(NetworkState.Status.FAILED, t.message!!))
                        }
                    }

                    override fun onResponse(call: Call<Feed>, response: Response<Feed>) {
                        if(response.isSuccessful) {
                            if(response.body()!!.articles != null) {
                                callback.onResult(response.body()?.articles!!, null, 2)
                            }
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            networkState.postValue(NetworkState(NetworkState.Status.FAILED, response.message()))
                        }
                    }
                })
        }

        override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, Article>) {
            Log.i(TAG, "Loading Rang " + params.key.toString() + " Count " + params.requestedLoadSize)
            networkState.postValue(NetworkState.LOADING)

            AppController.restApi.fetchFeed(QUERY, API_KEY, params.key, params.requestedLoadSize)
                .enqueue(object : Callback<Feed> {
                    override fun onFailure(call: Call<Feed>, t: Throwable) {
                        if(t.message != null) {
                            networkState.postValue(NetworkState(NetworkState.Status.FAILED, t.message!!))
                        }
                    }

                    override fun onResponse(call: Call<Feed>, response: Response<Feed>) {
                        if(response.isSuccessful) {
                            val nextKey = if (params.key == response.body()!!.totalResults) null else params.key + 1
                            if(response.body()?.articles != null) {
                                callback.onResult(response.body()?.articles!!, nextKey)
                            }
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            networkState.postValue(NetworkState(NetworkState.Status.FAILED, response.message()))
                        }
                    }

                })
        }

        override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, Article>) {
            TODO("Not yet implemented")
        }
    }
    
# Step 4: Setup the DataSourceFactory

    class FeedDataFactory : DataSource.Factory<Long, Article>() {

        val mutableLiveData: MutableLiveData<FeedDataSource> by lazy {
            MutableLiveData<FeedDataSource>()
        }

        val feedDataSource: FeedDataSource by lazy {
            FeedDataSource()
        }

        override fun create(): DataSource<Long, Article> {
            mutableLiveData.postValue(feedDataSource)
            return feedDataSource
        }
    }
    
# Step 5: Setup the ViewModel

    class FeedViewModel : ViewModel() {

        private val executor: Executor by lazy {
            Executors.newFixedThreadPool(5)
        }

        var networkState: LiveData<NetworkState>? = null

        var articleLivaData: LiveData<PagedList<Article>>? = null

        init {
            val feedDataFactory = FeedDataFactory()
            networkState = Transformations.switchMap(feedDataFactory.mutableLiveData) {
                it.networkState
            }
            val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(10)
                .setPageSize(20)
                .build()
            articleLivaData = LivePagedListBuilder(feedDataFactory, pagedListConfig)
                .setFetchExecutor(executor)
                .build()

        }

    }
 # Step 6: Setup the PagedListAdapter
 
     class FeedListAdapter : PagedListAdapter<Article, RecyclerView.ViewHolder> {

        companion object {
            private val TYPE_PROGRESS = 0
            private val TYPE_ITEM = 1
        }

        private var networkState: NetworkState? = null

        lateinit var context: Context

        constructor(context: Context) : super(Article.DIFF_CALLBACK) {
            this.context = context
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            if(viewType == TYPE_PROGRESS) {
                val headerBinding = NetworkItemBinding.inflate(layoutInflater, parent, false)
                val viewHolder = NetworkStateItemViewHolder(headerBinding)
                return viewHolder
            } else {
                val itemBinding = FeedItemBinding.inflate(layoutInflater, parent, false)
                val viewHolder = ArticleItemViewHolder(itemBinding)
                return viewHolder
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is ArticleItemViewHolder) {
                if(getItem(position) != null) {
                    holder.bindTo(getItem(position)!!)
                }
            } else if (holder is NetworkStateItemViewHolder) {
                if(networkState != null) {
                    holder.bindView(networkState!!)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            if(hasExtraRow() && position == itemCount - 1) {
                return TYPE_PROGRESS
            } else {
                return TYPE_ITEM
            }
        }

        private fun hasExtraRow() : Boolean {
            return networkState != null && networkState != NetworkState.LOADED
        }

        fun setNetworkState(newNetworkState: NetworkState) {
            var previousState = this.networkState
            var previousExtraRow = hasExtraRow()
            this.networkState = newNetworkState
            var newExtraRow = hasExtraRow()
            if(previousExtraRow != newExtraRow) {
                if(previousExtraRow) {
                    notifyItemRemoved(itemCount)
                } else {
                    notifyItemInserted(itemCount)
                }
            } else if(newExtraRow && previousState != newNetworkState) {
                notifyItemChanged(itemCount - 1)
            }
        }

        inner class ArticleItemViewHolder(val binding: FeedItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bindTo(article: Article) {
                binding.itemImage.visibility = View.VISIBLE
                binding.itemDesc.visibility = View.VISIBLE
                val author = if (article.author == null || article.author.isEmpty()) context.getString(R.string.author_name) else article.author
                if(article.title != null) {
                    val titleString = String.format(context.getString(R.string.item_title), author, article.title)
                    val spannableString = SpannableString(titleString)
                    spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context.applicationContext, R.color.secondary_text)),
                        titleString.lastIndexOf(author) + author.length + 1,
                        titleString.lastIndexOf(article.title) - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.itemTitle.text = spannableString
                    binding.itemTime.text = java.lang.String.format(
                        context.getString(R.string.item_date),
                        AppUtils.getDate(article.publishedAt),
                        AppUtils.getTime(article.publishedAt)
                    )
                }
                binding.itemDesc.text = article.description
                Picasso.get().load(article.urlToImage).resize(250, 200).into(binding.itemImage)
            }
        }

        inner class NetworkStateItemViewHolder(val binding: NetworkItemBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bindView(networkState: NetworkState) {
                if(networkState.status == NetworkState.Status.RUNNING) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                }
                if(networkState.status == NetworkState.Status.FAILED) {
                    binding.errorMsg.visibility = View.VISIBLE
                } else {
                    binding.errorMsg.visibility = View.GONE
                }
            }
        }

    }
    
# Step 7: Setup the Activity

    class MainActivity : AppCompatActivity() {

      private lateinit var adapter: FeedListAdapter
      private lateinit var feedViewModel: FeedViewModel
      private lateinit var binding: FeedActivityBinding

      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          //step 1: Using DataBinding, we setup the layout for the activity
          binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

          //Step 2: Initialize the ViewModel
          feedViewModel = FeedViewModel()

          //Step 3: Setup the adapter class for the RecyclerView
          binding.listFeed.layoutManager = LinearLayoutManager(applicationContext)
          adapter = FeedListAdapter(applicationContext)

          //Step 4: When a new page is available, we call submitList() method
          //of the PagedListAdapter class
          feedViewModel.articleLivaData?.observe(this, Observer<PagedList<Article>> {
              adapter.submitList(it)
          })

          feedViewModel.networkState?.observe(this, Observer<NetworkState> {
              adapter.setNetworkState(it)
          })
          binding.listFeed.adapter = adapter
      }
  }

