package cl.alkewallet.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cl.alkewallet.appContainer
import cl.alkewallet.databinding.FragmentTransactionsBinding

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WalletViewModel by activityViewModels {
        requireContext().appContainer().viewModelFactory
    }
    private val adapter = TransactionAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        binding.rvTransactions.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.transactions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.isVisible = list.isEmpty()
        }
        viewModel.loading.observe(viewLifecycleOwner) { binding.swipeRefresh.isRefreshing = it }
        viewModel.offline.observe(viewLifecycleOwner) { binding.tvOffline.isVisible = it }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvTransactions.adapter = null
        _binding = null
    }
}
