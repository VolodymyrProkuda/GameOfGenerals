package gogfmc.gog.GameOfGeneralForKids

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import gogfmc.gog.GameOfGen.GameGeneralViewModel
import gogfmc.gog.GameOfGeneralForKids.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    val viewModel : GameGeneralViewModel by viewModels()

    lateinit var binding: ActivityMainBinding
    var dImageList : MutableList<ImageView> = mutableListOf()
    var dDrawablesList = listOf(R.drawable.a15, R.drawable.a14,R.drawable.a1, R.drawable.a2,R.drawable.a3, R.drawable.a4,R.drawable.a5, R.drawable.a6,
                                 R.drawable.a7, R.drawable.a8,R.drawable.a9, R.drawable.a10,R.drawable.a11, R.drawable.a12,R.drawable.a13)
    var dDrawablesChooseList = listOf(R.drawable.b15, R.drawable.b14,R.drawable.b1, R.drawable.b2,R.drawable.b3, R.drawable.b4,R.drawable.b5, R.drawable.b6,
        R.drawable.b7, R.drawable.b8,R.drawable.b9, R.drawable.b10,R.drawable.b11, R.drawable.b12,R.drawable.b13)

    var etap = 0
    var currentPosToSituate = -1
    var listForCancel = MutableList<Int>(21){-1}
    var canMoveUp = -10
    var canMoveDown = -10
    var canMoveRight = -10
    var canMoveLeft = -10
    var choose = false
    var choosenField = -10

    data class Pairs(var from:Int, var to:Int)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle("Гра Генерали")
        binding.buttonSetSoldat.isEnabled = false
        binding.buttonCancelMove.isEnabled = false
        binding.textViewHint.text = "Вибери в меню (три крапочки)\n СТВОРИТИ ГРУ чи ПРИЄДНАТИСЯ, \n а потім Натисни на кнопку РОЗМІСТИТИ СОЛДАТ "

        dotImageListadd()

        viewModel.onMoveListener()




        for (i in 0..71)
            dImageList[i].setOnClickListener{
                if ((etap==1)&&(i>44)) {
                    if ((currentPosToSituate<20)&&(viewModel.troopsField[i] == -1)) {
                        currentPosToSituate++
                        dImageList[i].setImageResource(dDrawablesList[viewModel.myTroops[currentPosToSituate]])
                        viewModel.troopsField[i] = viewModel.myTroops[currentPosToSituate]
                        listForCancel[currentPosToSituate] = i

                    }
                    if (currentPosToSituate==20) binding.buttonSetSoldat.text = "Тож \n почнемо!"
                }
                if (etap==2){
                    if ((!choose)&&(viewModel.troopsField[i] != -1)) {
                    dImageList[i].setImageResource(dDrawablesChooseList[viewModel.troopsField[i]])
                    canMoveUp = i-9; if (canMoveUp<0) canMoveUp = -10
                    canMoveDown = i+9; if (canMoveDown>71) canMoveDown = -10
                    canMoveRight = i+1; if (i.mod(9) == 8) canMoveRight = -10
                    canMoveLeft = i-1; if (i.mod(9) == 0) canMoveLeft = -10
                        choosenField = i
                        choose = true
                    }else {
                        if (((i==canMoveUp)||(i==canMoveDown)||(i==canMoveLeft)||(i==canMoveRight))&&(viewModel.troopsField[i] == -1)) {
                            dImageList[i].setImageResource(dDrawablesList[viewModel.troopsField[choosenField]])
                            viewModel.movesMemory.add(Pairs(choosenField,i))
                           // binding.textViewHint.text = "from $choosenField to $i ; size ${viewModel.movesMemory.size}"
                            viewModel.troopsField[i] = viewModel.troopsField[choosenField]
                            viewModel.troopsField[choosenField] = -1
                            dImageList[choosenField].setImageResource(R.drawable.ab)
                            canMoveUp =  -10
                            canMoveDown =  -10
                            canMoveRight =  -10
                            canMoveLeft =  -10
                            choosenField = -10
                            choose = false
                            viewModel.movesMemoryInMongoDB.add(viewModel.pairsToMongoData(viewModel.movesMemory.last()))
                            viewModel.onMoveExecute()
                        }

                    }
                }


            }

    binding.buttonAbout.setOnClickListener {
        val aboutIntent = Intent(this,RulesActivity::class.java)
        startActivity(aboutIntent)
    }

    binding.buttonSetSoldat.setOnClickListener {
        if (etap==0) {binding.textViewHint.text = "Натискай почерзі на клітинки на \n перших трьох лініях, \n розміщуючи почерзі солдат \n починаючи із самого високого звання "; etap=1}
        if ((etap==1)&&(currentPosToSituate==20)) {
            onStartBattle()
            if (viewModel.serverReady || viewModel.clientReady) {etap = 2; binding.textViewHint.text = "ходи! "} else binding.textViewHint.text = "Противник не готовий! "

        }

    }
    binding.buttonCancelMove.setOnClickListener {
            if ((currentPosToSituate>-1) && (etap ==1)) {
                dImageList[listForCancel[currentPosToSituate]].setImageResource(R.drawable.ab)
                viewModel.troopsField[listForCancel[currentPosToSituate ]] = -1
                listForCancel[currentPosToSituate] = -1
                currentPosToSituate--
                binding.buttonSetSoldat.text = "Розмістити \n солдат"
            }
            if ((etap==2)&&(viewModel.movesMemory.size > 0)){
                dImageList[viewModel.movesMemory.last().from].setImageResource(dDrawablesList[viewModel.troopsField[viewModel.movesMemory.last().to]])
                viewModel.troopsField[viewModel.movesMemory.last().from] = viewModel.troopsField[viewModel.movesMemory.last().to]

                //binding.textViewHint.text = "movesMemory[movesMemory.size-1].from ${movesMemory.last().from} viewModel.troopsField[movesMemory[movesMemory.size-1].from] ${viewModel.troopsField[movesMemory.last().from]}"

                dImageList[viewModel.movesMemory.last().to].setImageResource(R.drawable.ab)
                viewModel.troopsField[viewModel.movesMemory.last().to] = -1
                viewModel.movesMemory.removeLast()
                viewModel.movesMemoryInMongoDB.removeLast()
            }


    }


        val bbb1 = Observer<MutableList<Int>> { t ->
            var str = ""
            for (tt in t) str += " ${tt}"
            binding.textViewHint.text = str
            if ((etap==2)) {//(!t.isEmpty())&&



                if (t.last() == viewModel.movesMemoryInMongoDB.last()) binding.textViewHint.text ="Ти походив!"

                if (t.last() != viewModel.movesMemoryInMongoDB.last()) {
                    binding.textViewHint.text = "Противник походив!"
                    viewModel.movesMemoryInMongoDB.add(t.last())
                    viewModel.movesMemory.add(viewModel.mongoToPairsData(t.last()))

                    dImageList[viewModel.movesMemory.last().to].setImageResource(dDrawablesList[viewModel.troopsField[viewModel.movesMemory.last().from]])
                    viewModel.troopsField[viewModel.movesMemory.last().to] =
                        viewModel.troopsField[viewModel.movesMemory.last().from]
                    dImageList[viewModel.movesMemory.last().from].setImageResource(R.drawable.ab)

                }

            }
        }

        viewModel.bbb.observe(this, bbb1)




    }
    fun dotImageListadd() {
        dImageList.add(binding.imgVW0)
        dImageList.add(binding.imgVW1)
        dImageList.add(binding.imgVW2)
        dImageList.add(binding.imgVW3)
        dImageList.add(binding.imgVW4)
        dImageList.add(binding.imgVW5)
        dImageList.add(binding.imgVW6)
        dImageList.add(binding.imgVW7)
        dImageList.add(binding.imgVW8)
        dImageList.add(binding.imgVW9)

        dImageList.add(binding.imgVW10)
        dImageList.add(binding.imgVW11)
        dImageList.add(binding.imgVW12)
        dImageList.add(binding.imgVW13)
        dImageList.add(binding.imgVW14)
        dImageList.add(binding.imgVW15)
        dImageList.add(binding.imgVW16)
        dImageList.add(binding.imgVW17)
        dImageList.add(binding.imgVW18)
        dImageList.add(binding.imgVW19)

        dImageList.add(binding.imgVW20)
        dImageList.add(binding.imgVW21)
        dImageList.add(binding.imgVW22)
        dImageList.add(binding.imgVW23)
        dImageList.add(binding.imgVW24)
        dImageList.add(binding.imgVW25)
        dImageList.add(binding.imgVW26)
        dImageList.add(binding.imgVW27)
        dImageList.add(binding.imgVW28)
        dImageList.add(binding.imgVW29)
        dImageList.add(binding.imgVW30)

        dImageList.add(binding.imgVW31)
        dImageList.add(binding.imgVW32)
        dImageList.add(binding.imgVW33)
        dImageList.add(binding.imgVW34)
        dImageList.add(binding.imgVW35)
        dImageList.add(binding.imgVW36)
        dImageList.add(binding.imgVW37)
        dImageList.add(binding.imgVW38)
        dImageList.add(binding.imgVW39)

        dImageList.add(binding.imgVW40)
        dImageList.add(binding.imgVW41)
        dImageList.add(binding.imgVW42)
        dImageList.add(binding.imgVW43)
        dImageList.add(binding.imgVW44)
        dImageList.add(binding.imgVW45)
        dImageList.add(binding.imgVW46)
        dImageList.add(binding.imgVW47)
        dImageList.add(binding.imgVW48)
        dImageList.add(binding.imgVW49)

        dImageList.add(binding.imgVW50)
        dImageList.add(binding.imgVW51)
        dImageList.add(binding.imgVW52)
        dImageList.add(binding.imgVW53)
        dImageList.add(binding.imgVW54)
        dImageList.add(binding.imgVW55)
        dImageList.add(binding.imgVW56)
        dImageList.add(binding.imgVW57)
        dImageList.add(binding.imgVW58)
        dImageList.add(binding.imgVW59)

        dImageList.add(binding.imgVW60)
        dImageList.add(binding.imgVW61)
        dImageList.add(binding.imgVW62)
        dImageList.add(binding.imgVW63)
        dImageList.add(binding.imgVW64)
        dImageList.add(binding.imgVW65)
        dImageList.add(binding.imgVW66)
        dImageList.add(binding.imgVW67)
        dImageList.add(binding.imgVW68)
        dImageList.add(binding.imgVW69)

        dImageList.add(binding.imgVW70)
        dImageList.add(binding.imgVW71)

        for (i in 0..71) dImageList[i].setImageResource(R.drawable.ab)

    }

    private lateinit var connectGame : MenuItem
    private lateinit var createGame : MenuItem
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.generals_menu, menu)

        connectGame = menu.findItem(R.id.connectGame);
        createGame = menu.findItem(R.id.createGame);

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.connectGame -> {
                viewModel.isServer = false
                binding.buttonSetSoldat.isEnabled = true
                binding.buttonCancelMove.isEnabled = true
                setTitle("Гра Генерали : приєднався")
             //   connectGame.setVisible(false);
             //   createGame.setVisible(false);

                true
            }
            R.id.createGame -> {
                viewModel.isServer = true
                binding.buttonSetSoldat.isEnabled = true
                binding.buttonCancelMove.isEnabled = true
                setTitle("Гра Генерали : розпочав")

            //   connectGame.setVisible(false);
             //   createGame.setVisible(false);
                true
            }
            R.id.clearField -> {
                viewModel.writeEmptyFieldToMongoDB()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    fun onStartBattle(){
        if (viewModel.isServer) {



        }else{

        }


        viewModel.writeFieldToMongoDB(viewModel.isServer)
/*
        viewModel.readFieldFromMongoDB(viewModel.isServer)
*/
        if (viewModel.clientReady) for (i in 0..26) {
            if (viewModel.troopsField[i]>=0) dImageList[i].setImageResource(R.drawable.enemy)//dDrawablesChooseList[viewModel.troopsField[i]]
        }
        if (viewModel.serverReady) for (i in 44..71) {
            if (viewModel.troopsField[i]>=0) dImageList[i].setImageResource(R.drawable.enemy)//dDrawablesChooseList[viewModel.troopsField[i]]
        }

    }

}