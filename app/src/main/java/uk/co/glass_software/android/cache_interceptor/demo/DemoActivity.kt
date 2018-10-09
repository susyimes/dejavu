package uk.co.glass_software.android.cache_interceptor.demo

import android.content.Context
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.multidex.MultiDex
import uk.co.glass_software.android.boilerplate.ui.mvp.MvpActivity
import uk.co.glass_software.android.boilerplate.utils.lambda.Callback1
import uk.co.glass_software.android.cache_interceptor.configuration.CacheInstruction
import uk.co.glass_software.android.cache_interceptor.configuration.CacheInstruction.Operation.*
import uk.co.glass_software.android.cache_interceptor.configuration.CacheInstruction.Operation.Expiring.*
import uk.co.glass_software.android.cache_interceptor.configuration.CacheInstruction.Operation.Type.*
import uk.co.glass_software.android.cache_interceptor.demo.DemoMvpContract.DemoMvpView
import uk.co.glass_software.android.cache_interceptor.demo.DemoMvpContract.DemoPresenter
import uk.co.glass_software.android.cache_interceptor.demo.injection.DemoViewModule
import uk.co.glass_software.android.cache_interceptor.demo.model.CatFactResponse
import uk.co.glass_software.android.cache_interceptor.demo.presenter.CompositePresenter.Method
import uk.co.glass_software.android.cache_interceptor.demo.presenter.CompositePresenter.Method.RETROFIT
import uk.co.glass_software.android.cache_interceptor.demo.presenter.CompositePresenter.Method.VOLLEY


internal class DemoActivity
    : MvpActivity<DemoMvpView, DemoPresenter, DemoMvpContract.DemoViewComponent>(),
        DemoMvpView,
        (String) -> Unit {

    private lateinit var listAdapter: ExpandableListAdapter

    private val loadButton by lazy { findViewById<View>(R.id.load_button)!! }
    private val refreshButton by lazy { findViewById<View>(R.id.refresh_button)!! }
    private val clearButton by lazy { findViewById<View>(R.id.clear_button)!! }
    private val offlineButton by lazy { findViewById<View>(R.id.offline_button)!! }
    private val invalidateButton by lazy { findViewById<View>(R.id.invalidate_button)!! }
    private val gitHubButton by lazy { findViewById<View>(R.id.github)!! }

    private val retrofitRadio by lazy { findViewById<View>(R.id.radio_button_retrofit)!! }
    private val volleyRadio by lazy { findViewById<View>(R.id.radio_button_volley)!! }

    private val freshOnlyCheckBox by lazy { findViewById<CheckBox>(R.id.checkbox_fresh_only)!! }
    private val compressCheckBox by lazy { findViewById<CheckBox>(R.id.checkbox_compress)!! }
    private val encryptCheckBox by lazy { findViewById<CheckBox>(R.id.checkbox_encrypt)!! }

    private val instructionView by lazy { findViewById<InstructionView>(R.id.instruction)!! }

    private val catFactView by lazy { findViewById<TextView>(R.id.fact)!! }
    private val listView by lazy { findViewById<ExpandableListView>(R.id.list)!! }

    private var encrypt: Boolean = false
    private var compress: Boolean = false
    private var freshOnly: Boolean = false

    private lateinit var presenterSwitcher: Callback1<Method>

    private var instructionType: CacheInstruction.Operation.Type = CACHE
    private var instructionIsAnnotation: Boolean = true

    override fun initialiseComponent() = DaggerDemoMvpContract_DemoViewComponent
            .builder()
            .demoViewModule(DemoViewModule(this, this))
            .build()
            .apply { presenterSwitcher = presenterSwitcher() }!!

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun invoke(p1: String) {
        listAdapter.log(p1)
    }

    override fun onCreateMvpView(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)

        loadButton.setOnClickListener { loadCatFact(false) }
        refreshButton.setOnClickListener { loadCatFact(true) }
        clearButton.setOnClickListener { clearEntries() }
        offlineButton.setOnClickListener { offline() }
        invalidateButton.setOnClickListener { invalidate() }

        retrofitRadio.setOnClickListener { switchPresenter(RETROFIT) }
        volleyRadio.setOnClickListener { switchPresenter(VOLLEY) }
        gitHubButton.setOnClickListener { openGithub() }

        freshOnlyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            freshOnly = isChecked
            updateInstructionView()
        }

        compressCheckBox.setOnCheckedChangeListener { _, isChecked ->
            compress = isChecked
            updateInstructionView()
        }

        encryptCheckBox.setOnCheckedChangeListener { _, isChecked ->
            encrypt = isChecked
            updateInstructionView()
        }

        listAdapter = ExpandableListAdapter(this) { catFactView.text = it }
        listView.setAdapter(listAdapter)

        listAdapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onInvalidated() {
                onChanged()
            }

            override fun onChanged() {
                for (x in 0 until listAdapter.groupCount) {
                    listView.expandGroup(x)
                }
            }
        })
    }

    private fun switchPresenter(method: Method) {
        updateInstructionType(method == RETROFIT)
        presenterSwitcher(method)
    }

    private fun loadCatFact(isRefresh: Boolean) {
        updateInstruction(if (isRefresh) REFRESH else CACHE)

        getPresenter().loadCatFact(
                isRefresh,
                encrypt,
                compress,
                freshOnly
        )
    }

    private fun clearEntries() {
        updateInstruction(CLEAR)
        getPresenter().clearEntries()
    }

    private fun offline() {
        updateInstruction(OFFLINE)
        getPresenter().offline(freshOnly)
    }

    private fun invalidate() {
        updateInstruction(INVALIDATE)
        getPresenter().invalidate()
    }

    private fun updateInstruction(type: CacheInstruction.Operation.Type) {
        this.instructionType = type
        updateInstructionView()
    }

    private fun updateInstructionType(isAnnotation: Boolean) {
        this.instructionIsAnnotation = isAnnotation
        updateInstructionView()
    }

    private fun updateInstructionView() {
        val configuration = getPresenter().configuration

        val operation = when (instructionType) {
            CACHE -> Cache(
                    configuration.cacheDurationInMillis,
                    freshOnly,
                    configuration.mergeOnNextOnError,
                    encrypt,
                    compress,
                    false
            )
            REFRESH -> Refresh(
                    configuration.cacheDurationInMillis,
                    freshOnly,
                    configuration.mergeOnNextOnError,
                    false
            )
            DO_NOT_CACHE -> DoNotCache
            INVALIDATE -> Invalidate
            OFFLINE -> Offline(
                    freshOnly,
                    configuration.mergeOnNextOnError
            )
            CLEAR,
            CLEAR_ALL -> Clear(clearOldEntriesOnly = false)
        }

        instructionView.setInstruction(
                CacheInstruction(
                        CatFactResponse::class.java,
                        operation
                ),
                instructionIsAnnotation
        )
    }

    override fun showCatFact(response: CatFactResponse) {
        listAdapter.showCatFact(response)
    }

    override fun onCallStarted() {
        listView.post {
            catFactView.text = ""
            setButtonsEnabled(false)
            listAdapter.onStart()
        }
    }

    override fun onCallComplete() {
        listView.post {
            setButtonsEnabled(true)
            listAdapter.onComplete()
        }
    }

    private fun setButtonsEnabled(isEnabled: Boolean) {
        loadButton.isEnabled = isEnabled
        refreshButton.isEnabled = isEnabled
        clearButton.isEnabled = isEnabled
        invalidateButton.isEnabled = isEnabled
        offlineButton.isEnabled = isEnabled
    }

    private fun openGithub() {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse("https://github.com/pthomain/RxCacheInterceptor"))
    }

}
