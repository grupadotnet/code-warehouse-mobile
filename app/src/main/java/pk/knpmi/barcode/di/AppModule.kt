package pk.knpmi.barcode.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pk.knpmi.barcode.data.repository.fake.FakeLocalisationRepository
import pk.knpmi.barcode.data.repository.fake.FakeProductRepository
import pk.knpmi.barcode.domain.repository.LocalisationRepository
import pk.knpmi.barcode.domain.repository.ProductRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocalisationRepository(): LocalisationRepository{
        return FakeLocalisationRepository()
    }

    @Provides
    @Singleton
    fun provideProductRepository(): ProductRepository{
        return FakeProductRepository()
    }
}