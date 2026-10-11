package io.nekohasekai.sagernet.ui.tv

/** One foreground check per real connection/profile/network epoch. No automatic reconnect. */
class TvHealthSession {
    data class Token(val generation: Long,val profile: Long)
    private var generation=0L
    private var key: Pair<Long,String>?=null
    private var issued=false
    private var complete=false
    fun sync(connected: Boolean,profile: Long,networkEpoch: String): Boolean {
        val next=if(connected&&profile>0) profile to networkEpoch else null
        if(next==key)return false
        key=next;generation++;issued=false;complete=false;return true
    }
    fun begin(force: Boolean=false): Token? {
        val current=key?:return null
        if(issued || complete&&!force)return null
        generation++;issued=true;complete=false
        return Token(generation,current.first)
    }
    fun accepts(token: Token)=issued&&token.generation==generation&&key?.first==token.profile
    fun finish(token: Token): Boolean {
        if(!accepts(token))return false
        issued=false;complete=true;return true
    }
    fun cancel(token: Token) {if(accepts(token)){generation++;issued=false;complete=false}}
}
