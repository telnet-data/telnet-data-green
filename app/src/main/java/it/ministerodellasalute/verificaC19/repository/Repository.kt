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
 *  Created by pedro_cecchini on 18/10/21, 12:18
 */

package it.ministerodellasalute.verificaC19.repository

import it.ministerodellasalute.verificaC19.api.WebApi
import it.ministerodellasalute.verificaC19.data.GreenPassRequest
import javax.inject.Inject

class Repository @Inject constructor(
    private val api: WebApi
) {

    suspend fun postGreenPass(req: GreenPassRequest){
        api.sendGreenPassResponse(req)
    }
}