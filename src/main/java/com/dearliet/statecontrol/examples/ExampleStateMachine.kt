package com.dearliet.statecontrol

import com.dearliet.statecontrol.models.*
import org.bukkit.Bukkit
import org.bukkit.Location

/*
*
*
* stateMachine.goto(State.TEST)
* parentStateMachine.goto(GameStateMachineStates.INGAME)
*
*
* */

class EnumeratedExample {

    data class GameObject(val location: Location) {
        var time = 20

        init {
            GameObjectStateMachine(this).start()
        }
    }

    private class GameObjectStateMachine(holder: GameObject)
        : StateMachine<GameObject, GameObjectStateMachine.StateType>(holder, null){

        enum class StateType { GAME_1, GAME_2, GAME_3 }

        override val startingStateKey: StateType
            get() = StateType.GAME_1

        override val initStates: LinkedHashMap<StateType, () -> State<GameObject>>
            get() = linkedMapOf(
                StateType.GAME_1 to { GameState(holder, this, 20) },
                StateType.GAME_2 to { GameObjectStateMachineGame(holder, this) },
                StateType.GAME_3 to { BattleState(holder, this) },
            )

        override fun onChangeState(previousStateKey: StateType) {
            super.onChangeState(previousStateKey)
            Bukkit.getLogger().info("|- " + currentStateKey.name)
        }
    }

    private class GameObjectStateMachineGame(holder: GameObject, override val parentStateMachine: GameObjectStateMachine)
        : LoopingStateMachine<GameObject, GameObjectStateMachineGame.StateType>(holder, parentStateMachine){

        enum class StateType { SUB_GAME_1, SUB_GAME_2, SUB_GAME_3 }

        override val initStates: LinkedHashMap<StateType, () -> State<GameObject>>
            get() = linkedMapOf(
                    StateType.SUB_GAME_1 to { GameState(holder, this, holder.time) },
                    StateType.SUB_GAME_2 to { GameState(holder, this, holder.time) },
                    StateType.SUB_GAME_3 to { GameState(holder, this, holder.time) },
            )

        override fun onDisable() {
            super.onDisable()
            holder.time += 20
        }

        override fun onChangeState(previousStateKey: StateType) {
            super.onChangeState(previousStateKey)
            Bukkit.getLogger().info(" |- " + currentStateKey.name)
        }
    }

    private open class GameState<T: Any>(holder: T, parentStateMachine: StateMachine<T, *>?, ticks: Int)
        : TimerState<T>(holder, parentStateMachine, ticks){

        override fun onEnable() {
            super.onEnable()
            Bukkit.getLogger().info("Hey")
        }
    }

    private class BattleState(holder: GameObject, override val parentStateMachine: GameObjectStateMachine)
        : State<GameObject>(holder, parentStateMachine){

        override fun onEnable() {
            super.onEnable()
            parentStateMachine.previous()
        }
    }
}