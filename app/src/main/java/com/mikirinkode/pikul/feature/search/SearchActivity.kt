package com.mikirinkode.pikul.feature.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikirinkode.pikul.data.model.PikulResult
import com.mikirinkode.pikul.databinding.ActivitySearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {

    private val binding: ActivitySearchBinding by lazy {
        ActivitySearchBinding.inflate(layoutInflater)
    }

    private val viewModel: SearchViewModel by viewModels()

    private val adapter : SearchResultAdapter by lazy {
        SearchResultAdapter()
    }

    private var currentSearchToken: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        viewModel.test()
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressed()
            }

            rvSearchResults.layoutManager = LinearLayoutManager(this@SearchActivity)
            rvSearchResults.adapter = adapter

            etSearch.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    val query = p0.toString().trim()

                    System.currentTimeMillis().apply {
                        currentSearchToken = this
                        Handler().postDelayed({
                            if (this != currentSearchToken) return@postDelayed
                            if (query.isNotBlank()) {
                                viewModel.search(query).observe(this@SearchActivity) { result ->
                                    when (result) {
                                        is PikulResult.Loading -> {}
                                        is PikulResult.LoadingWithProgress -> {} // TODO
                                        is PikulResult.Error -> {}
                                        is PikulResult.Success -> {
                                            adapter.setData(result.data)
                                        }
                                    }
                                }
                            }
                        }, 500)
                    }
                }
            })
        }
    }
}