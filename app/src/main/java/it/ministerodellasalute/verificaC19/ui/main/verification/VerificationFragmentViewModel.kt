/*
 *  ---license-start
 *  eu-digital-green-certificates / dgca-verifier-app-android
 *  ---
 *  Copyright (C) 2021 T-Systems International GmbH and all other contributors
 *  ---
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ---license-end
 *
 *  Created by pedro_cecchini on 18/10/21, 12:33
 */

package it.ministerodellasalute.verificaC19.ui.main.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.ministerodellasalute.verificaC19.data.GreenPassRequest
import it.ministerodellasalute.verificaC19.repository.Repository
import it.ministerodellasalute.verificaC19.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerificationFragmentViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {

    private val serverResponseFlow = MutableStateFlow<Resource<String>>(Resource.Success(""))
    val serverResponse: Flow<Resource<String>> = serverResponseFlow

    suspend fun sendDataToServer(req: GreenPassRequest){
        viewModelScope.launch {
            val serverResp = repository.postGreenPass(req)
            serverResponseFlow.value = serverResp
        }
    }
}