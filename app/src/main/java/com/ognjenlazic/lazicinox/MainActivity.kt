package com.ognjenlazic.lazicinox

import android.app.AlertDialog
import android.media.CamcorderProfile
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri


const val BUTTOM_SHEET_PEEK_HIGHT = 50f
private const val DOUBLE_TAP_TOLRANCE_MS = 1000L



class MainActivity : AppCompatActivity() {


    lateinit var arFragment: ArFragment

    private val models = mutableListOf(
        Model(R.drawable.modeljedan , "Model 1", R.raw.modeljedan),
        Model(R.drawable.modeldva , "Model 2", R.raw.modeldva),
        Model(R.drawable.modeltri , "Model 3", R.raw.modeltri),
        Model(R.drawable.modlcetiri , "Model 4", R.raw.modelcetiri),
        Model(R.drawable.modelpet , "Model 5", R.raw.modelpet),
        Model(R.drawable.modelsest , "Model 6", R.raw.modelsest),
        Model(R.drawable.modelsedam , "Model 7", R.raw.modelsedam),
        Model(R.drawable.modelosam , "Model 8", R.raw.modelosam),
        Model(R.drawable.modelsavijenadva , "Model 7", R.raw.modelsavijenadva),
        Model(R.drawable.modelsastaklomjedan , "Model 8", R.raw.modelsastaklomjedan),
        Model(R.drawable.modelsastaklomdva , "Model 9", R.raw.modelsastaklomdva),
        Model(R.drawable.modelsastaklomtri , "Model 10", R.raw.modelsastaklomtri),
        Model(R.drawable.modelrukohvatjedan , "Rukohvat 1", R.raw.modelrukohvatjedan),
        Model(R.drawable.modelrukohvatdva , "Rukohvat 2", R.raw.modelrukohvatdva),
        Model(R.drawable.kapijajedan , "Kapija 1", R.raw.kapijajedan),
        Model(R.drawable.kapijadva , "Kapija 2", R.raw.kapijadva),
        Model(R.drawable.kapijatri , "Kapija 3", R.raw.kapijatri)




    )


    private lateinit var selectedModel: Model


    val viewNodes = mutableListOf<Node>()

    private lateinit var photoSaver: PhotoSaver
    private lateinit var videoRecorder: VideoRecorder

    private var isRecording = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = fragment as ArFragment
        setupBottomSheet()
        setupRecyclerView()
        setupDubleTapArPlane()
        setupFab()

        photoSaver = PhotoSaver(this)
        videoRecorder = VideoRecorder(this).apply {
            sceneView = arFragment.arSceneView
            setVideoQuality(CamcorderProfile.QUALITY_1080P, resources.configuration.orientation)
        }

        getCurrentScene().addOnUpdateListener {
            rotateViewNodesTowardsUser()
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Detektujte ravnu povr??inu za postavljanje modela i pojavi??e se bijele ta??ke na povr??ini. \n" +
                "Dvokliknite na povr??inu i pojavi??e se model na detektovanoj povr??ini. \n" +
                "Model mo??ete uve??avati ili umanjivati po potrebi. \n" +
                "Model mo??ete pomijerati po detektovanoj povr??ini. \n" +
                "Ako ??elite da izbri??ete model, jednom kliknite na njega i pojavi??e se crveno dugme \"OBRI??I MODEL\".\n" +
                "Klikom na crveno dugme bi??ete model. \n" +
                "Mogu??e je postaviti vi??e modela na istu povr??inu. \n" +
                "Modele birate tako ??to izvu??ete meni modela koji se nalazi na dnu ekrana. \n" +
                "Klikom na dugme sa kamerom mo??ete da slikate model u realnom okru??enju, du??im pritiskom na isto dugme model mo??ete snimati. \n"
        )
            .setTitle("Uputstvo")
            .setNegativeButton("OK") { dialog, id ->

            }

        val alert: AlertDialog = builder.create()
        alert.show()

        fab2.setOnClickListener { val i = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.inox.rs.ba"))
            startActivity(i) }

    }

    private fun setupFab() {
        fab.setOnClickListener {
            if (!isRecording){
                photoSaver.takePhoto(arFragment.arSceneView)

            }

        }
        fab.setOnLongClickListener {
            isRecording = videoRecorder.toggleRecordingState()
            true
        }
        fab.setOnTouchListener { view, motionEvent ->
            if(motionEvent.action == MotionEvent.ACTION_UP && isRecording){
                isRecording = videoRecorder.toggleRecordingState()
                Toast.makeText(this, "Video je sa??uvan.", Toast.LENGTH_LONG).show()
                true
            } else false

        }
    }

    private fun setupDubleTapArPlane(){
        var firstTapTime = 0L
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if(firstTapTime == 0L){
                firstTapTime = System.currentTimeMillis()
            } else if ( System.currentTimeMillis() - firstTapTime < DOUBLE_TAP_TOLRANCE_MS) {
                firstTapTime = 0L

            loadModel{
                modelRenderable, viewRenderable ->
                addNodeToScene(hitResult.createAnchor(), modelRenderable, viewRenderable)
            }
            } else  {
                firstTapTime = System.currentTimeMillis()

            }
        }
    }

    private fun setupRecyclerView(){

        rvModels.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvModels.adapter = ModelAdapter(models).apply {
            selectedModel.observe(this@MainActivity, Observer {
                this@MainActivity.selectedModel = it
                val newTitle = "${it.title}"
                tvModel.text = newTitle
            })
        }
    }

    private fun setupBottomSheet(){
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                BUTTOM_SHEET_PEEK_HIGHT,
                resources.displayMetrics
            ).toInt()


        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheet.bringToFront()
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })
    }

    private fun getCurrentScene () = arFragment.arSceneView.scene


    private fun createDeletButton(): Button {
        return  Button(this).apply {
            text = "Obri??i model"
            setBackgroundColor(android.graphics.Color.RED)
            setTextColor(android.graphics.Color.WHITE)
        }
    }

    private fun rotateViewNodesTowardsUser(){
        for (node in viewNodes)
            node.renderable?.let {
                val camPos = getCurrentScene().camera.worldPosition
                val viewNodePos = node.worldPosition
                val dir = Vector3.subtract(camPos, viewNodePos)
                node.worldRotation = Quaternion.lookRotation(dir, Vector3.up())
            }
    }



    private fun addNodeToScene (
        anchor: Anchor,
        modelRenderable: ModelRenderable,
        viewRenderable: ViewRenderable
    ){
        val anchorNode = AnchorNode(anchor)
        val modelNode = TransformableNode(arFragment.transformationSystem).apply {
            renderable = modelRenderable
            setParent(anchorNode)
            getCurrentScene().addChild(anchorNode)
            select()
        }

        val viewNode = Node().apply {
            renderable = null
            setParent(modelNode)
            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3(0f, box.size.y, 0f)
            (viewRenderable.view as Button).setOnClickListener {
                getCurrentScene().removeChild(anchorNode)
                viewNodes.remove(this)
            }
        }

        viewNodes.add(viewNode)

        modelNode.setOnTapListener{
            _, _ ->
            if (!modelNode.isTransforming ){
                if (viewNode.renderable == null) {
                    viewNode.renderable = viewRenderable
                } else {
                    viewNode.renderable = null
                }
            }
        }
    }




    private fun loadModel(callback: (ModelRenderable, ViewRenderable) -> Unit){
        val modelRenderable = ModelRenderable.builder()
            .setSource(this, selectedModel.modekResourceId)
            .build()

        val viewRenderable = ViewRenderable.builder()
            .setView(this, createDeletButton())
            .build()
        CompletableFuture.allOf(modelRenderable, viewRenderable)
            .thenAccept{
                callback(modelRenderable.get(), viewRenderable.get())

            }
            .exceptionally {
                Toast.makeText(this, "Gre??ka prilikom u??itavanja modela $it", Toast.LENGTH_LONG).show()
                null
            }
    }
}
