package com.yashovardhan99.healersdiary.online

import android.content.Context
import com.yashovardhan99.core.OnlineModuleDependencies
import com.yashovardhan99.healersdiary.online.importFirebase.ImportFirebaseFragment
import dagger.BindsInstance
import dagger.Component

@Component(dependencies = [OnlineModuleDependencies::class])
interface OnlineComponent {
    fun inject(fragment: ImportFirebaseFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun appDependencies(onlineModuleDependencies: OnlineModuleDependencies): Builder
        fun build(): OnlineComponent
    }
}