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
 *  Created by danielsp on 8/17/21, 4:37 PM
 */

package it.ministerodellasalute.verificaC19.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy

@Dao
interface PassDao {
    @Query("SELECT COUNT(id) FROM passes")
    fun getCount(): Int

    @Query("SELECT * FROM passes")
    fun getAllPasses(): List<Pass>

    @Query("SELECT * FROM passes WHERE id IN (:passIds)")
    fun getAllPassesByIds(passIds: Array<String?>): List<Pass>

    @Query("SELECT * FROM passes WHERE id LIKE :id LIMIT 1")
    fun getPassById(id: Int?): Pass?

    @Query("SELECT * FROM passes WHERE hash LIKE :hash LIMIT 1")
    fun getPassByHash(hash: String?): Pass?

    @Query("DELETE FROM passes WHERE id = :id")
    fun deletePassById(id: String?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPass(pass: Pass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPasses(passes: MutableList<Pass>)

    @Delete
    fun deletePass(pass: Pass)
}