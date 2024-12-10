package com.example.utslecture.Setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.SimpleExpandableListAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.utslecture.R

class Help : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_help, container, false)
        val backButton = view.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        val expandableListView = view.findViewById<ExpandableListView>(R.id.expandableListView)
        val questions = listOf(
            "Bagaimana cara menggunakan aplikasi ini?",
            "Apa saja fitur yang tersedia?",
            "Bagaimana cara mengubah pengaturan akun?",
            "Apakah data saya aman di aplikasi ini?",
            "Bagaimana cara melaporkan masalah atau bug?",
            "Apakah aplikasi ini memerlukan koneksi internet?",
            "Bagaimana cara memperbarui aplikasi ke versi terbaru?"
        )

        val answers = mapOf(
            questions[0] to listOf("Untuk menggunakan aplikasi ini, Anda dapat mulai dengan melakukan pendaftaran atau login jika sudah memiliki akun. Setelah masuk, Anda dapat menavigasi melalui menu untuk mengakses berbagai fitur yang tersedia."),
            questions[1] to listOf("Aplikasi ini memiliki beberapa fitur utama seperti search blog, bookmark blog, AI, dan pengaturan akun."),
            questions[2] to listOf("Untuk mengubah pengaturan akun, buka menu 'Pengaturan' dan pilih 'Pengaturan Akun'. Anda dapat mengubah informasi pribadi, kata sandi, dan preferensi lainnya di sana."),
            questions[3] to listOf("Kami sangat menghargai keamanan dan privasi pengguna. Data Anda disimpan secara aman dan hanya digunakan sesuai ketentuan layanan."),
            questions[4] to listOf("Jika Anda menemukan masalah atau bug, Anda dapat melaporkannya melalui menu 'Bantuan' atau kirimkan email kepada kami di support@aplikasianda.com."),
            questions[5] to listOf("Beberapa fitur dalam aplikasi ini memerlukan koneksi internet, namun ada beberapa fitur yang dapat digunakan secara offline."),
            questions[6] to listOf("Untuk memperbarui aplikasi ke versi terbaru, buka Play Store, cari aplikasi ini, dan pilih 'Perbarui' jika ada versi terbaru yang tersedia.")
        )

        val childData = answers.map { entry ->
            entry.value.map { mapOf("Jawaban" to it) }
        }

        val adapter = SimpleExpandableListAdapter(
            requireContext(),
            questions.map { mapOf("Pertanyaan" to it) },
            R.layout.group_item,  // Use the custom layout for the group
            arrayOf("Pertanyaan"),
            intArrayOf(R.id.groupTextView),  // Reference the TextView ID in group_item.xml
            childData,
            R.layout.child_item,  // Use the custom layout for the child
            arrayOf("Jawaban"),
            intArrayOf(R.id.childTextView)  // Reference the TextView ID in child_item.xml
        )

        expandableListView.setAdapter(adapter)
        return view
    }
}
