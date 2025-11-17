package ee.ut.cs.iotbazaar.ui.item

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import ee.ut.cs.iotbazaar.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkConstructor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class ItemViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ItemViewModel
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        mockkConstructor(ItemRepository::class)
        coEvery { anyConstructed<ItemRepository>().getAllItems() } returns flowOf(emptyList())
        coEvery { anyConstructed<ItemRepository>().insert(any(), any()) } returns Result.success("new-id")

        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = ItemViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addItem should call repository's insert method`() = runTest {
        // 1. Arrange
        val itemName = "Unit Test Item"
        val isReserved = false

        // 2. Act
        viewModel.addItem(itemName, isReserved)

        // 3. Assert
        coVerify(exactly = 1) {
            anyConstructed<ItemRepository>().insert(name = itemName, reserved = isReserved)
        }
    }
}
