package com.dearliet.statecontrol.models

abstract class LoopingStateMachine<T : Any>(holder: T, parentStateMachine: StateMachine<T>?, val loopCount: Int) : StateMachine<T>(holder, parentStateMachine) {
    val isLastLoop get() = currentLoop == loopCount
    var currentLoop = 1
        private set

    fun restart(){
        currentLoop = 1
        start()
    }

    open fun onLoopComplete(){}

    override fun onExceedUpperStateBoundary() {
        onLoopComplete()
        if(++currentLoop > loopCount && loopCount > 0) {
            super.onExceedUpperStateBoundary()
        } else start()
    }
}