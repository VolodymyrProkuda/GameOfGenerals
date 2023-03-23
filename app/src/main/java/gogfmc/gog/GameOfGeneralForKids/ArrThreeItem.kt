package gogfmc.gog.GameOfGeneralForKids

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class ArrThreeItem() : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.create()
    var name : String = "moves"
    var valuList : RealmList<Int> = realmListOf()
}