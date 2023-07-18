package com.mikirinkode.pikul.feature.chat.extra

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mikirinkode.pikul.R
import com.mikirinkode.pikul.databinding.FragmentFullScreenImageBinding
import com.mikirinkode.pikul.databinding.FragmentMessageInfoBinding
import com.mikirinkode.pikul.utils.DateHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullScreenImageFragment : Fragment() {
    private var _binding: FragmentFullScreenImageBinding? = null
    private val binding get() = _binding!!
    private val args: FullScreenImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFullScreenImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        onClickAction()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        binding.apply {
            tvMessageOnDetailImage.text = args.messageOnImage

            tvUserName.text = args.senderName
            tvDate.text =
                DateHelper.getRegularFormattedDateTimeFromTimestamp(args.dateSent)

            Glide.with(requireContext())
                .load(args.imageUrl)
                .into(ivDetailImage)
        }
    }


    private fun onClickAction(){
        binding.apply {
            btnBack.setOnClickListener {
                Navigation.findNavController(binding.root).navigateUp()
            }
        }
    }
}