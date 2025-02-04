/*
 * Copyright (c) 2022 Frederik Enste <frederik@fenste.de>.
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fenste.ms.address.application.controllers

import de.fenste.ms.address.application.dtos.requests.CreateCityDto
import de.fenste.ms.address.application.dtos.requests.UpdateCityDto
import de.fenste.ms.address.application.dtos.responses.CityDto
import de.fenste.ms.address.application.services.CityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
@Suppress("unused")
class CityController(
    @Autowired private val cityService: CityService,
) {
    @SchemaMapping(field = "cities", typeName = "Query")
    fun cities(
        @Argument limit: Int? = null,
        @Argument offset: Int? = null,
    ): List<CityDto>? = cityService.list(
        limit = limit,
        offset = offset?.toLong(),
    )

    @SchemaMapping(field = "city", typeName = "Query")
    fun city(
        @Argument id: String,
    ): CityDto? = cityService.find(
        id = UUID.fromString(id),
    )

    @SchemaMapping(field = "createCity", typeName = "Mutation")
    fun createCity(
        @Argument create: CreateCityDto,
    ): CityDto = cityService.create(
        create = create,
    )

    @SchemaMapping(field = "updateCity", typeName = "Mutation")
    fun updateCity(
        @Argument update: UpdateCityDto,
    ): CityDto = cityService.update(
        update = update,
    )

    @SchemaMapping(field = "deleteCity", typeName = "Mutation")
    fun deleteCity(
        @Argument id: String,
    ): Boolean = cityService.delete(
        id = UUID.fromString(id),
    )
}
