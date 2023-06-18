package com.example.a0509.ui.notice

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a0509.Notice
import com.example.a0509.NoticeAdapter
import com.example.a0509.NoticeViewActivity
import com.example.a0509.databinding.FragmentNoticeBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException


class NoticeFragment : Fragment() {

    private var _binding: FragmentNoticeBinding? = null

    private val binding get() = _binding!!
    
    private lateinit var noticeRecyclerView: RecyclerView
    private lateinit var noticeAdapter: NoticeAdapter
    private lateinit var noticeList: MutableList<Notice>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoticeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        
        return root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        noticeRecyclerView = binding.noticeRecyclerView
        noticeRecyclerView.layoutManager =LinearLayoutManager(requireContext())
        
        getNoticeList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    private fun disableSSLCertificateChecking() {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<out java.security.cert.X509Certificate>? {
                return null
            }
            
            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                p0: Array<out java.security.cert.X509Certificate>?,
                arg1: String?
            ) {
                // Not implemented
            }
            
            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                p0: Array<out java.security.cert.X509Certificate>?,
                arg1: String?
            ) {
                // Not implemented
            }
        })
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }
    
    private fun getNoticeList() {
        CoroutineScope(Dispatchers.Main).launch {
            noticeList = getNotice()
            noticeAdapter= NoticeAdapter(noticeList)
            noticeRecyclerView.adapter=noticeAdapter
            updateRecyclerView()
        }
    }
    
    private suspend fun getNotice(): MutableList<Notice> = withContext(Dispatchers.IO) {
        val url="https://sogang.ac.kr/front/boardlist.do?currentPage=%d&menuGubun=1&siteGubun=1&bbsConfigFK=2&searchField=ALL&searchValue=&searchLowItem=ALL"
        val noticeList=mutableListOf<Notice>()
        try {
            disableSSLCertificateChecking()
            
            // 상단 고정 공지 먼저 추출
            var docs=Jsoup.connect(url.format(1)).get()
            var urlElements=docs.select("tr.notice").select("div.subject").select("a[href]")
            var titleElements=docs.select("tr.notice").select("div.subject").select("span.text")
            var dateElements=docs.select("tr.notice").select("td:eq(4)")
            for(j in urlElements.indices) {
                val urlStr=urlElements[j].absUrl("href")
                val titleStr="★ ${titleElements[j].text()}"
                val dateStr=dateElements[j].text()
                noticeList.add(Notice(urlStr,titleStr,dateStr))
            }
            
            var i=1
            var flag=false
            val curDate= CalendarDay.today()
            val minDateStr: String
            // 현재 달로부터 6개월 전부터 현재까지 올라온 공지만 리스트에 저장
            if(curDate.month<=6) minDateStr="%04d.%02d.%02d".format(curDate.year-1,curDate.month+6,curDate.day)
            else minDateStr="%04d.%02d.%02d".format(curDate.year,curDate.month-6,curDate.day)
            while(!flag) {
                docs = Jsoup.connect(url.format(i++)).get()
                urlElements = docs.select("tr:not(.notice)").select("div.subject").select("a[href]")
                titleElements = docs.select("tr:not(.notice)").select("div.subject").select("span.text")
                dateElements = docs.select("tr:not(.notice)").select("td:eq(4)")
                for (j in urlElements.indices) {
                    val urlStr = urlElements[j].absUrl("href")
                    val titleStr = titleElements[j].text()
                    val dateStr = dateElements[j].text()
                    
                    if(dateStr.compareTo(minDateStr)<0) {
                        flag=true
                        break
                    }
                    noticeList.add(Notice(urlStr, titleStr, dateStr))
                }
            }
        } catch(exception:Exception) {
            exception.printStackTrace()
        }
        noticeList
    }
    
    private fun updateRecyclerView() {
        noticeAdapter.setOnItemClickListener(object: NoticeAdapter.OnItemClickListener {
            override fun onItemClick(v: View, pos: Int) {
                val intent=Intent(requireActivity(),NoticeViewActivity::class.java)
                intent.putExtra("url",noticeList[pos].url)
                startActivity(intent)
            }
        })
        noticeRecyclerView.adapter=noticeAdapter
    }
}