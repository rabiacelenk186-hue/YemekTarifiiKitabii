package com.example.yemektarifiikitabii.view

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.yemektarifiikitabii.databinding.FragmentTarifBinding

class TarifFragment : Fragment() {
    private var _binding: FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher : ActivityResultLauncher<R.string>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var secilenGorsel: Uri?= null
    private var secilenBitmap: Bitmap?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(
            requireContext(),
            TarifDatabase::class.java, "Tarifler"
        ) //.allowMainThreadQueries()
            .build()

        tarifDao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater, container, false)
        val view = binding.root
        return view

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kaydetButton.setOnClickListener { kaydet(it) }
        binding.silButton.setOnClickListener { sil(it) }
        binding.imageView.setOnClickListener { gorselSec(it) }

        arguments?.let {
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi
            if (bilgi == "yeni") {
                //Yeni kaydediliyor
                binding.silButton.isEnabled = false
                binding.kaydetButton.isEnabled = true
                binding.isimText.setText("")
                binding.malzemeText.setText("")
            } else {
                //Eski gösteriliyor
                binding.silButton.isEnabled = true
                binding.kaydetButton.isEnabled = false
                val id = TarifFragmentArgs.fromBundle(it).id
                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse))

            }
        }
    }

    private fun handleResponse(tarif: Tarif) {
        tarifFromListe = tarif
        binding.isimText.setText(tarif.isim)
        binding.malzemeText.setText(tarif.malzeme)
        val byteDizisi = tarif.gorsel
        val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
        binding.imageView.setImageBitmap(bitmap)
    }

    fun kaydet(view: View) {
        val isim = binding.isimText.text.toString()
        val malzeme = binding.malzemeText.text.toString()

        if (secilenBitmap != null) {

            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            val tarif = Tarif(isim = isim, malzeme = malzeme, gorsel = byteDizisi)

            mDisposable.add(
                tarifDao.insert(tarif)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsertionAndDeletion)
            )
        }

    }

    private fun handleResponseForInsertionAndDeletion() {
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }


    fun sil(view: View) {
        tarifFromListe?.let {
            mDisposable.add(
                tarifDao.delete(tarifFromListe!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseForInsertionAndDeletion)
            )
        }
    }

    fun gorselSec(view: View) {
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity().applicationContext,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Permission needed for gallery",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Give Permission",
                            View.OnClickListener {
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }).show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                } else {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)

                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        requireActivity().applicationContext,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        Snackbar.make(
                            view,
                            "Permission needed for gallery",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction("Give Permission",
                            View.OnClickListener {
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }).show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                } else {
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)

                }
            }
        }
    }


    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    secilenGorsel = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(
                                requireActivity().contentResolver,
                                secilenGorsel!!
                            )
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        } else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                secilenGorsel
                            )
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result ->
            if (result) {
                //permission granted
                val intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(requireContext(), "Permisson needed!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    private fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int) : Bitmap {

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani: Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1) {
            // görselimiz yatay
            width = maximumBoyut
            val kisaltilmisHeight = width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        } else {
            //görselimiz dikey
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()

        }


        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width, height, true)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}