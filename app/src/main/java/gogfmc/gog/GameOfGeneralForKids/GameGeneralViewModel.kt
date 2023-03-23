package gogfmc.gog.GameOfGen

import gogfmc.gog.GameOfGeneralForKids.ArrThreeItem
import gogfmc.gog.GameOfGeneralForKids.MainActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.kotlin.ext.query
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.internal.platform.runBlocking
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.notifications.SingleQueryChange
import io.realm.kotlin.notifications.UpdatedObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class GameGeneralViewModel : ViewModel() {

    var movesMemory : MutableList<MainActivity.Pairs> = mutableListOf()
    var movesMemoryInMongoDB : MutableList<Int> = mutableListOf()
    var isServer = false
    var serverReady = false
    var clientReady = false
    val troopsField = MutableList<Int>(72){-1}
    val myTroops = listOf<Int>(14,13,12,11,10,9,8,7,6,5,4,3,2,2,2,2,2,2,1,1,0)

  //  var b = mutableListOf<Int>(12,13,14)
    var bb : MutableList<Int> = mutableListOf(0,0,0)
    var bbb = MutableLiveData<MutableList<Int>>()
    val app = App.create("data-rpxli")
    init{

        bbb.value = mutableListOf(1,1,1)

    }
    fun pairsToMongoData(mp: MainActivity.Pairs): Int{

        return mp.from*1000+mp.to
    }
    fun mongoToPairsData(i: Int): MainActivity.Pairs{
        var mp1: MainActivity.Pairs = MainActivity.Pairs(0,0)
        mp1.from = i.div(1000).toInt()
        mp1.to = i.mod(1000).toInt()
        return mp1
    }

    fun rotateFieldForOpponent(): MutableList<Int> {
        val troopsFieldOpponent = MutableList<Int>(72){-1}

        for (i in 0..71) troopsFieldOpponent[i] = troopsField[71-i]
        return troopsFieldOpponent

    }
    fun writeEmptyFieldToMongoDB(){
        runBlocking {
            val user = app.login(Credentials.anonymous())
            val config =
                SyncConfiguration.Builder(user, "Part2", setOf(ArrThreeItem::class))
                    // specify name so realm doesn't just use the "default.realm" file for this user
                    .waitForInitialRemoteData(timeout = 2.seconds)//
                    .name("Part2")
                    .build()
            val realm = Realm.open(config)

            // b = mutableListOf<Int>(12,13,14)
            // b.add(4)
            realm.write{
                val aTI : ArrThreeItem? = this.query<ArrThreeItem>("name == 'field'").first().find()
                aTI?.valuList = MutableList<Int>(72){-1}.toRealmList()
            }


            realm.close()
        }
    }

    fun writeFieldToMongoDB(isServer: Boolean){

        runBlocking {
            val user = app.login(Credentials.anonymous())
            val config =
                SyncConfiguration.Builder(user, "Part2", setOf(ArrThreeItem::class))
                    // specify name so realm doesn't just use the "default.realm" file for this user
                    .waitForInitialRemoteData()//timeout = 2.seconds
                    .name("Part2")
                    .build()
            val realm = Realm.open(config)

            // b = mutableListOf<Int>(12,13,14)
            // b.add(4)
            val aTII : ArrThreeItem? = realm.query<ArrThreeItem>("name == 'field'").first().find()
            val currentFieldState = aTII?.valuList
            if (isServer) for (i in 0..26) if (currentFieldState!!.get(i) >-1) clientReady = true
            if (isServer) if (clientReady) for (i in 0..26) troopsField[i] = currentFieldState!!.get(i)

            if (!isServer) for (i in 44..71) if (currentFieldState!!.get(i) >-1) serverReady = true
            if (!isServer) if (serverReady) for (i in 44..71) troopsField[i] = currentFieldState!!.get(i)


            realm.write{
                val aTI : ArrThreeItem? = this.query<ArrThreeItem>("name == 'field'").first().find()
                if (isServer) aTI?.valuList = troopsField.toRealmList()
                else aTI?.valuList = rotateFieldForOpponent().toRealmList()
            }


            realm.close()
        }
   }
    fun readFieldFromMongoDB(isServer: Boolean){
        runBlocking {
            val user = app.login(Credentials.anonymous())
            val config =
                SyncConfiguration.Builder(user, "Part2", setOf(ArrThreeItem::class))
                    // specify name so realm doesn't just use the "default.realm" file for this user
                    .waitForInitialRemoteData(timeout = 3.seconds)//
                    .name("Part2")
                    .build()
            val realm = Realm.open(config)

                val aTI : ArrThreeItem? = realm.query<ArrThreeItem>("name == 'field'").first().find()
                for (i in 0..26) troopsField[i] = aTI!!.valuList[i]
           realm.close()
        }

    }

    fun onMoveExecute() {
        //  val app = App.create("data-rpxli")
        runBlocking {
            val user = app.login(Credentials.anonymous())
            val config =
                SyncConfiguration.Builder(user, "Part2", setOf(ArrThreeItem::class))
                    // specify name so realm doesn't just use the "default.realm" file for this user
                    .waitForInitialRemoteData(timeout = 3.seconds)
                    .name("Part2")
                    .build()
            val realm = Realm.open(config)

            // b = mutableListOf<Int>(12,13,14)
            // b.add(4)

            realm.write{
                val aTI : ArrThreeItem? = this.query<ArrThreeItem>("name == 'moves'").first().find()
                aTI?.valuList = movesMemoryInMongoDB.toRealmList()
            }


            realm.close()
        }
    }

    fun onMoveListener(){

        //val app = App.create("data-rpxli")


        val job = CoroutineScope(Dispatchers.Default).launch {
            // create a Flow from the Item collection, then add a listener to the Flow

            val user = app.login(Credentials.anonymous())
            val config =
                SyncConfiguration.Builder(user, "Part2", setOf(ArrThreeItem::class))
                    .name("Part2")
                    .build()
            val realm = Realm.open(config)
            val querObjA = realm.query(ArrThreeItem::class,"name == 'moves'").first()
            val itemsFlow = querObjA.asFlow()
            itemsFlow.collect { changes: SingleQueryChange<ArrThreeItem> ->
                when (changes) {

                    is UpdatedObject -> {
                        changes.changedFields
                        changes.obj
                        changes.isFieldChanged("name")
                        bb = changes.obj.valuList
                        bbb.postValue(bb)

                    }
                    else -> {
                        // types other than UpdatedResults are not changes -- ignore them
                    }

                }


            }


            //realm.close()
        }
        //job.cancel()
    }



}
