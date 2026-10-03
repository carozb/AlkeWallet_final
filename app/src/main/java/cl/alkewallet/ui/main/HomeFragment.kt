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
import cl.alkewallet.databinding.FragmentHomeBinding
import cl.alkewallet.ui.ImageLoader
import cl.alkewallet.util.Formatters
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WalletViewModel by activityViewModels {
        requireContext().appContainer().viewModelFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.profile.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe
            binding.tvName.text = user.username
            binding.tvEmail.text = user.email
            binding.tvBalance.text = Formatters.money(user.balance)
            ImageLoader.loadAvatar(binding.ivAvatar, user.avatarUrl)
        }
        viewModel.offline.observe(viewLifecycleOwner) { binding.tvOffline.isVisible = it }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.logout) { _, _ -> viewModel.logout() }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
