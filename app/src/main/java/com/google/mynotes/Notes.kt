package com.google.mynotes

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.mynotes.databinding.ActivityNotesBinding
import com.google.mynotes.databinding.OpenNotesBinding
import com.google.mynotes.databinding.UpdateNotesBinding
import kotlinx.coroutines.launch

class Notes : AppCompatActivity() {


    private var binding: ActivityNotesBinding ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding?.root)



        val NotesDao = (application as NotesApp).db.NotesDao()
        binding?.idFABAdd?.setOnClickListener {
            NewPoemDialog(NotesDao)
        }


        lifecycleScope.launch{
            NotesDao.fetchAllNotes().collect{
                val list = ArrayList(it)
                setupListOfDateINtoRecycleVIew(list,NotesDao)
            }
        }

    }


    private fun NewPoemDialog(NotesDao: NotesDao){
        val  PoemDialog = Dialog(this)
        PoemDialog.setCancelable(false)
        PoemDialog.setContentView(R.layout.notes_add_dialog)



        val cancelBtn = PoemDialog.findViewById<Button>(R.id.idBtnCancel)
        val addBtn = PoemDialog.findViewById<Button>(R.id.idBtnAdd)
        val itemTopic = PoemDialog.findViewById<EditText>(R.id.idTopic)
        val PoemDes = PoemDialog.findViewById<EditText>(R.id.idnotes)



        cancelBtn.setOnClickListener {
            PoemDialog.dismiss()

        }

        addBtn.setOnClickListener {
            val itemTopic: String = itemTopic.text.toString()
            val PoemDes: String = PoemDes.text.toString()

            if(itemTopic.isNotEmpty() && PoemDes.isNotEmpty()){
             lifecycleScope.launch{
            NotesDao.insert(NotesEntity(Topic = itemTopic, Poem = PoemDes))
                 Toast.makeText(applicationContext,"Record saved",Toast.LENGTH_LONG).show()
             }
                PoemDialog.dismiss()
            }else{
                Toast.makeText(this,"field cannot be blank",Toast.LENGTH_LONG).show()
            }

        }

        PoemDialog.show()

    }


    private fun setupListOfDateINtoRecycleVIew(NotesList: ArrayList<NotesEntity>,
                                               NotesDao: NotesDao){
        if(NotesList.isNotEmpty()){
            val itemAdapter = itemAdapter(NotesList,
                {
                        updateId ->
                    updateRecordDialog(updateId,NotesDao)
                },
                {
                        deleteId ->
                    deleteRecordAlertDialog(deleteId,NotesDao)
                },
                {
                    OpenId ->
                    openNotes(OpenId,NotesDao)
                },
                {
                        ShareId ->
                    ShareNotes(ShareId,NotesDao)
                },
            )
            binding?.rvItemsPoem?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsPoem?.adapter = itemAdapter
            binding?.rvItemsPoem?.visibility = View.VISIBLE
            binding?.tvNoDataAvailable?.visibility = View.GONE
        }else{
            binding?.rvItemsPoem?.visibility = View.GONE
            binding?.tvNoDataAvailable?.visibility = View.VISIBLE
        }
    }

    private fun ShareNotes(id: Int, NotesDao: NotesDao) {

        var Topic :String = "hs"
        var PoemDes =String()

        lifecycleScope.launch{
            NotesDao.fetchNotesById(id).collect {
                if (it != null) {
                    Topic = it.Topic
                    PoemDes =it.Poem

                    Toast.makeText(applicationContext,"${Topic} and ${PoemDes} and",Toast.LENGTH_LONG).show()
                    val sendIntent = Intent()
                    sendIntent.type = "text/plain"
                    sendIntent.action = Intent.ACTION_SEND
                    val body = "The Poem Topic is = ${Topic}\n"+
                            "--------------------------------\n"+
                            "${PoemDes}"
                    sendIntent.putExtra(Intent.EXTRA_TEXT,body)
                    Intent.createChooser(sendIntent, "Share using")
                    startActivity(sendIntent)
                }
            }

        }



//        Toast.makeText(applicationContext,"${Topic} and ${PoemDes} and",Toast.LENGTH_LONG).show()




    }


    private fun openNotes(id:Int,NotesDao: NotesDao){
//        val OpenDialog = Dialog(this)
//        OpenDialog.setCancelable(false)
//        val binding = OpenNotesBinding.inflate(layoutInflater)
//        OpenDialog.setContentView(binding.root)
        var Topic = ""
        var PoemDes = ""

        lifecycleScope.launch{
            NotesDao.fetchNotesById(id).collect {
                if (it != null) {
                    Topic = it.Topic
                    PoemDes = it.Poem

                    val intent = Intent(this@Notes,OpenPoem::class.java)
                    intent.putExtra(Constants.POEM_TOPIC,Topic)
                    intent.putExtra(Constants.POEM_DES,PoemDes)
                    startActivity(intent)

//                    binding.tvPoemTopic.setText(it.Topic)
//                    binding.tvPoemDes.setText(it.Poem)
                }
            }

        }

//
//        binding.btnClose.setOnClickListener {
//           OpenDialog.dismiss()
//        }
//        OpenDialog.show()
    }


    private fun updateRecordDialog(id:Int,NotesDao: NotesDao){
        val updateDialog = Dialog(this)
        updateDialog.setCancelable(false)
        val binding = UpdateNotesBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch{
            NotesDao.fetchNotesById(id).collect {
                if (it != null) {
                    binding.etPoemTopic.setText(it.Topic)
                    binding.etUpdatePoem.setText(it.Poem)
                }
            }

        }
        binding.btnUpdatePoem.setOnClickListener{
            val Topic = binding.etPoemTopic.text.toString()
            val Poem = binding.etUpdatePoem.text.toString()
            if(Topic.isNotEmpty() && Poem.isNotEmpty()){
                lifecycleScope.launch{
                    NotesDao.update(NotesEntity(id,Topic,Poem))
                    Toast.makeText(applicationContext,"Record Updated",Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()
                }
            }else{
                Toast.makeText(applicationContext,"filed cannot be blank",Toast.LENGTH_LONG).show()
            }
        }

        binding.btnCancelPoem.setOnClickListener {
            updateDialog.dismiss()
        }
        updateDialog.show()
    }


    private fun deleteRecordAlertDialog(id:Int,employeeDao: NotesDao){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

//        builder.setIcon(android)

        builder.setPositiveButton("Yes"){
                dialogInterface,_ ->
            lifecycleScope.launch{
                employeeDao.delete(NotesEntity(id))
                Toast.makeText(applicationContext,"Record deleted successfully",Toast.LENGTH_LONG).show()
            }
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("No"){
                dialogInterface,which->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    }









