package cl.alkewallet.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cl.alkewallet.R
import cl.alkewallet.appContainer
import cl.alkewallet.databinding.FragmentSendBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SendFragment : Fragment() {

    private var _binding: FragmentSendBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WalletViewModel by activityViewModels {
        requireContext().appContainer().viewModelFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSend.setOnClickListener {
            viewModel.send(
                binding.etAmount.text.toString(),
                binding.etDescription.text.toString()
            )
        }

        viewModel.sendErrors.observe(viewLifecycleOwner) { errors ->
            binding.tilAmount.error = errors.amount
            binding.tilDescription.error = errors.description
        }
        viewModel.sending.observe(viewLifecycleOwner) { sending ->
            binding.progress.isVisible = sending
            binding.btnSend.isEnabled = !sending
        }
        viewModel.sent.observe(viewLifecycleOwner) { event ->
            if (event.getContentIfNotHandled() != null) {
                binding.etAmount.text?.clear()
                binding.etDescription.text?.clear()
                requireActivity().findViewById<BottomNavigationView>(R.id.bottom_nav)
                    .selectedItemId = R.id.nav_transactions
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
