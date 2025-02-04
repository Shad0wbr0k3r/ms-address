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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.infrastructure.tables.StateTable
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class StateRepositoryTest(
    @Autowired private val repository: StateRepository,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`(): Unit = transaction {
        val expected = SampleData.states.sortedBy { s -> s.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`(): Unit = transaction {
        val expected = SampleData.states
            .sortedBy { s -> s.name }
            .drop(2)
            .take(1)
        val actual = repository.list(
            order = arrayOf(StateTable.name to SortOrder.ASC),
            offset = 2,
            limit = 1,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on no data`(): Unit = transaction {
        SampleData.clear()
        val list = repository.list()

        assertTrue(list.empty())
    }

    @Test
    fun `test find by id on sample data`(): Unit = transaction {
        val expected = SampleData.states.random()
        val actual = repository.find(id = expected.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by id on no data`(): Unit = transaction {
        SampleData.clear()
        val actual = repository.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test find by id on non existing sample data`(): Unit = transaction {
        val actual = repository.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test create`(): Unit = transaction {
        val name = "Name"
        val country = SampleData.countries.random()

        val actual = repository.create(
            name = name,
            countryId = country.id.value,
        )

        assertNotNull(actual.id)
        assertEquals(name, actual.name)
        assertEquals(country, actual.country)
    }

    @Test
    fun `test create existing`(): Unit = transaction {
        val name = "Berlin"
        val country = SampleData.countries.first()

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                name = name,
                countryId = country.id.value,
            )
        }
    }

    @Test
    fun `test create non existing country`(): Unit = transaction {
        val name = "Name"
        val countryId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                name = name,
                countryId = countryId,
            )
        }
    }

    @Test
    fun `test update name`(): Unit = transaction {
        val sampleId = SampleData.states.random().id.value
        val name = "Name"

        val actual = repository.update(
            id = sampleId,
            name = name,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
    }

    @Test
    fun `test update country`(): Unit = transaction {
        val sampleId = SampleData.states.first().id.value
        val countryId = SampleData.countries.last().id.value

        val actual = repository.update(
            id = sampleId,
            countryId = countryId,
        )

        assertNotNull(actual)
        assertEquals(countryId, actual.country.id.value)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val sampleId = SampleData.states.first().id.value
        val name = "Name"
        val countryId = SampleData.countries.last().id.value

        val actual = repository.update(
            id = sampleId,
            name = name,
            countryId = countryId,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(countryId, actual.country.id.value)
    }

    @Test
    fun `test update all to same`(): Unit = transaction {
        val sample = SampleData.states.random()
        val name = sample.name
        val countryId = sample.country.id.value

        val actual = repository.update(
            id = sample.id.value,
            name = name,
            countryId = countryId,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(countryId, actual.country.id.value)
    }

    @Test
    fun `test update nothing`(): Unit = transaction {
        val expected = SampleData.states.random()
        val actual = repository.update(id = expected.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = id,
                name = "doesn't matter",
                countryId = UUID.randomUUID(),
            )
        }
    }

    @Test
    fun `test update name to existing`(): Unit = transaction {
        val sampleId = SampleData.states.first().id.value
        val name = SampleData.states.drop(1).first().name

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = sampleId,
                name = name,
            )
        }
    }

    @Test
    fun `test update country to not existing`(): Unit = transaction {
        val sampleId = SampleData.states.first().id.value
        val countryId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = sampleId,
                countryId = countryId,
            )
        }
    }

    @Test
    @Ignore // TODO allow cascade deletion?
    fun `test delete`(): Unit = transaction {
        val sampleId = SampleData.states.random().id.value

        repository.delete(sampleId)

        assertNull(Country.findById(sampleId))
    }

    @Test
    fun `test delete not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.delete(id)
        }
    }
}
