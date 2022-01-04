package com.tcn.bicicas.utils

import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.test.KoinTestRule

inline fun <reified T : Any> KoinTestRule.get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = koin.get(qualifier, parameters)

