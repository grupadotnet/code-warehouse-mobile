package pk.knpmi.barcode.data.repository.fake

import pk.knpmi.barcode.domain.model.Localisation
import pk.knpmi.barcode.domain.model.Product

/**
 * A tiny in-memory "database" shared by fake repositories.
 *
 * This makes the whole flow testable without a backend:
 * UI -> ViewModel -> UseCase -> Repository (fake) -> in-memory store.
 */
object FakeInMemoryStore {

    // Stores products in RAM (acts like a table).
    val products: MutableList<Product> =
        mutableListOf(
            Product(
                id = "P-0001",
                name = "Mleko 2%",
                category = "Nabiał",
                localisationId = "L-0100",
                quantity = 12.0,
                date = 1710000000L,
            ),
            Product(
                id = "P-0002",
                name = "Chleb pszenny",
                category = "Pieczywo",
                localisationId = "L-0101",
                quantity = 5.0,
                date = 1710000100L,
            ),
            Product(
                id = "P-0003",
                name = "Woda gazowana",
                category = "Napoje",
                localisationId = "L-0100",
                quantity = 24.0,
                date = 1710000200L,
            ),
        )

    // Stores locations in RAM (acts like a table).
    val localisations: MutableList<Localisation> =
        mutableListOf(
            Localisation(id = "L-0100", name = "Regał A1"),
            Localisation(id = "L-0101", name = "Regał A2"),
            Localisation(id = "L-0102", name = "Magazyn (tył)"),
        )
}

