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
import de.fenste.ms.address.infrastructure.tables.CountryTable
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
class CountryRepositoryTest(
    @Autowired private val repository: CountryRepository,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`(): Unit = transaction {
        val expected = SampleData.countries.sortedBy { c -> c.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`(): Unit = transaction {
        val expected = SampleData.countries
            .sortedBy { c -> c.alpha2 }
            .drop(2)
            .take(1)
        val actual = repository.list(
            order = arrayOf(CountryTable.alpha2 to SortOrder.ASC),
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
        val expected = SampleData.countries.random()
        val actual = repository.find(id = expected.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by alpha2 on sample data`(): Unit = transaction {
        val expected = SampleData.countries.random()
        val actual = repository.find(alpha2 = expected.alpha2)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by alpha3 on sample data`(): Unit = transaction {
        val expected = SampleData.countries.random()
        val actual = repository.find(alpha3 = expected.alpha3)

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
    fun `test find by nothing on sample data`(): Unit = transaction {
        assertFailsWith<IllegalArgumentException> {
            repository.find()
        }
    }

    @Test
    fun `test create`(): Unit = transaction {
        val alpha2 = "CZ"
        val alpha3 = "CZE"
        val name = "Czechia"
        val localizedName = "Tschechien"

        val actual = repository.create(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual.id)
        assertEquals(alpha2, actual.alpha2)
        assertEquals(alpha3, actual.alpha3)
        assertEquals(name, actual.name)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test create existing alpha2`(): Unit = transaction {
        val alpha2 = SampleData.countries.random().alpha2
        val alpha3 = "XXX"
        val name = "Name"
        val localizedName = "LocalizedName"

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test create existing alpha3`(): Unit = transaction {
        val alpha2 = "XX"
        val alpha3 = SampleData.countries.random().alpha3
        val name = "Name"
        val localizedName = "LocalizedName"

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test create existing name`(): Unit = transaction {
        val alpha2 = "XX"
        val alpha3 = "XXX"
        val name = SampleData.countries.random().name
        val localizedName = "LocalizedName"

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test update alpha2`(): Unit = transaction {
        val sampleId = SampleData.countries.random().id.value
        val alpha2 = "XX"

        val actual = repository.update(
            id = sampleId,
            alpha2 = alpha2,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
    }

    @Test
    fun `test update alpha3`(): Unit = transaction {
        val sampleId = SampleData.countries.random().id.value
        val alpha3 = "XXX"

        val actual = repository.update(
            id = sampleId,
            alpha3 = alpha3,
        )

        assertNotNull(actual)
        assertEquals(alpha3, actual.alpha3)
    }

    @Test
    fun `test update name`(): Unit = transaction {
        val sampleId = SampleData.countries.random().id.value
        val name = "Name"

        val actual = repository.update(
            id = sampleId,
            name = name,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
    }

    @Test
    fun `test update localizedName`(): Unit = transaction {
        val sampleId = SampleData.countries.random().id.value
        val localizedName = "LocalizedName"

        val actual = repository.update(
            id = sampleId,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val sampleId = SampleData.countries.random().id.value
        val alpha2 = "XX"
        val alpha3 = "XXX"
        val name = "Name"
        val localizedName = "LocalizedName"

        val actual = repository.update(
            id = sampleId,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
        assertEquals(alpha3, actual.alpha3)
        assertEquals(name, actual.name)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test update all to same`(): Unit = transaction {
        val sample = SampleData.countries.random()
        val alpha2 = sample.alpha2
        val alpha3 = sample.alpha3
        val name = sample.name
        val localizedName = sample.localizedName

        val actual = repository.update(
            id = sample.id.value,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
        assertEquals(alpha3, actual.alpha3)
        assertEquals(name, actual.name)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test update nothing`(): Unit = transaction {
        val expected = SampleData.countries.random()
        val actual = repository.update(id = expected.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = id,
                name = "doesn't",
                localizedName = "matter",
            )
        }
    }

    @Test
    fun `test update alpha2 to existing`(): Unit = transaction {
        val sampleId = SampleData.countries.first().id.value
        val alpha2 = SampleData.countries.last().alpha2

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = sampleId,
                alpha2 = alpha2,
            )
        }
    }

    @Test
    fun `test update alpha3 to existing`(): Unit = transaction {
        val sampleId = SampleData.countries.first().id.value
        val alpha3 = SampleData.countries.last().alpha3

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = sampleId,
                alpha3 = alpha3,
            )
        }
    }

    @Test
    fun `test update name to existing`(): Unit = transaction {
        val sampleId = SampleData.countries.first().id.value
        val name = SampleData.countries.last().name

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = sampleId,
                name = name,
            )
        }
    }

    @Test
    @Ignore // TODO allow cascade deletion?
    fun `test delete`(): Unit = transaction {
        val sampleId = SampleData.countries.random().id.value

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
