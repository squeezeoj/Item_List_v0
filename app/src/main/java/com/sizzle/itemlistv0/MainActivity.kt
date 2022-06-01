package com.sizzle.itemlistv0

//------------------------------------------------------------------
// Imports
//------------------------------------------------------------------
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sizzle.itemlistv0.ui.theme.ItemListV0Theme

//------------------------------------------------------------------
// Items
//------------------------------------------------------------------
data class Item(
    var id: Int,
    var title: String
)

//------------------------------------------------------------------
// Main View Model
//------------------------------------------------------------------
class MainViewModel {

    //------------------------------------------------------
    // Items Data
    //------------------------------------------------------
    var allItems: List<Item>
    var filtering: Boolean
    var filteredItems: List<Item>
    var specificItem: Item

    //------------------------------------------------------
    // Initialize View Model
    //------------------------------------------------------
    init {

        val item01 = Item(id = 1, title = "First Item")
        val item02 = Item(id = 2, title = "Second Item")
        val item03 = Item(id = 3, title = "Third Item")

        allItems = mutableListOf(item01, item02, item03)
        filtering = false
        filteredItems = emptyList()
        specificItem = Item(id = 0, title = "Initial Item")

    }   // End Initializer

    //------------------------------------------------------
    // Item Methods
    //------------------------------------------------------
    fun insertItem(item: Item) {
        allItems = allItems + item
    }

    fun updateItem(item: Item) {
        allItems.find { it.id == item.id }?.title = item.title
    }

    fun filterItems(filterTitleText: String) {
        filteredItems = allItems.filter { it.title.contains(filterTitleText) }
    }

    fun getItemByID(id: Int) {
        allItems.forEach {
            if (it.id == id) {
                specificItem.id = it.id
                specificItem.title = it.title
            }
        }
    }

    fun deleteItem(item: Item) {
        allItems = allItems - item
    }

}   // End Main View Model


//------------------------------------------------------------------
// Navigation Routes
//------------------------------------------------------------------
sealed class NavRoutes(val route: String) {
    object ItemListScreen : NavRoutes("itemList")
    object ItemDetailScreen : NavRoutes("itemDetail")
}

//------------------------------------------------------------------
// Main Activity
//------------------------------------------------------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ItemListV0Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppSetup()
                }
            }
        }
    }
}

//------------------------------------------------------------------
// Setup App
//------------------------------------------------------------------
@Composable
fun AppSetup(
    viewModel: MainViewModel = MainViewModel()
) {

    //--- Navigation Setup
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NavRoutes.ItemListScreen.route,
    ) {

        //--- Item List Screen
        composable(NavRoutes.ItemListScreen.route) {
            ItemListScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        //--- Item Detail Screen
        composable(NavRoutes.ItemDetailScreen.route + "/{id}" + "/{crud}") { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")
            val crud = backStackEntry.arguments?.getString("crud")
            ItemDetailScreen(
                navController = navController,
                viewModel = viewModel,
                id = id,
                crud = crud
            )
        }

    }	// End Nav Host

}

//------------------------------------------------------------------
// Items List Screen
//------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    var checkedState by remember { mutableStateOf(viewModel.filtering) }

    Column {

        //----------------------------------------------------------
        // Title
        //----------------------------------------------------------
        Text(
            text = "Item List V0",
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )

        Spacer(modifier = Modifier.height(5.dp)); Divider(); Spacer(modifier = Modifier.height(5.dp))

        //----------------------------------------------------------
        // Control Row
        //----------------------------------------------------------
        Row {

            //------------------------------------------------------
            // Filter Box
            //------------------------------------------------------
            val itemFilter: String
            var textFilter by remember { mutableStateOf(TextFieldValue("")) }
            OutlinedTextField(
                value = textFilter,
                modifier = Modifier.width(100.dp),
                label = { Text(text = "Filter") },
                singleLine = true,
                onValueChange = {
                    textFilter = it

                    if (checkedState) {
                        viewModel.filterItems(it.text)
                    } else {
                        // Nothing
                    }

                }
            )
            itemFilter = textFilter.text

            Spacer(Modifier.width(10.dp))

            //------------------------------------------------------
            // Filter Checkbox
            //------------------------------------------------------
            Checkbox(
                checked = checkedState,
                onCheckedChange = {
                    checkedState = it
                    viewModel.filtering = !viewModel.filtering
                    viewModel.filterItems(itemFilter)
                }
            )

            Spacer(Modifier.width(20.dp))

            //------------------------------------------------------
            // Add New Item Button
            //------------------------------------------------------
            Button(onClick = {
                navController.navigate(
                    route = NavRoutes.ItemDetailScreen.route
                            + "/" + "0"
                            + "/" + "CREATE"
                )
            }) {
                Text(
                    text = "New Item",
                    modifier = Modifier
                        .height(40.dp)
                        .align(Alignment.CenterVertically),
                    style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.background)
                )
            }

        }

        Spacer(modifier = Modifier.height(5.dp)); Divider(); Spacer(modifier = Modifier.height(5.dp))

        //------------------------------------------------------------------
        // List Items
        //------------------------------------------------------------------

        val myItems: List<Item> = if (viewModel.filtering) {
            viewModel.filteredItems
        } else {
            viewModel.allItems
        }

        myItems.forEach {item ->
            ClickableText(
                text = AnnotatedString("Item: ${item.title}"),
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                onClick = {
                    viewModel.getItemByID(item.id)
                    navController.navigate(
                        route = NavRoutes.ItemDetailScreen.route
                                + "/" + item.id
                                + "/" + "UPDATE"
                    )
                }	// End On Click
            )	// End Clickable Text
            Spacer(modifier = Modifier.height(5.dp)); Divider(); Spacer(modifier = Modifier.height(5.dp))
        }	// End All Items For Each

    }	// End Column

}	// End Item List Screen


//------------------------------------------------------------------
// Items Detail Screen
//------------------------------------------------------------------
@Composable
fun ItemDetailScreen(
    navController: NavHostController,
    viewModel: MainViewModel,
    id: Int? = 0,
    crud: String? = "NONE"
) {

    val itemID: String
    var itemTitle: String
    var myItem: Item

    when (crud) {
        //---------------------------------------------------------
        // Create New Item
        //---------------------------------------------------------
        "CREATE" -> {

            Column {

                //--- Item Title
                var textTitle by remember { mutableStateOf(TextFieldValue("")) }
                OutlinedTextField(
                    value = textTitle,
                    label = { Text(text = "Title") },
                    singleLine = true,
                    onValueChange = {
                        textTitle = it
                    }
                )
                itemTitle = textTitle.text

                //--- Create New Item from Parts
                myItem = Item(
                    id = (100000..200000).random(),
                    title = itemTitle
                )

                //--- Add New Item Button
                Button(
                    onClick = {
                        viewModel.insertItem(myItem)
                        navController.navigate(route = NavRoutes.ItemListScreen.route) {
                            popUpTo(NavRoutes.ItemListScreen.route)
                        }
                    }) {
                    Text(
                        text = "Submit",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.background),
                    )
                }    // End Add New Item Button

            }    // End Create New Item Column

        }
        //---------------------------------------------------------
        // Update Existing Item
        //---------------------------------------------------------
        "UPDATE" -> {

            if (id != null) {
                viewModel.getItemByID(id)

                myItem = viewModel.specificItem

                itemID = myItem.id.toString()
                itemTitle = myItem.title

                Column {

                    //--- Item ID Text Field
                    OutlinedTextField(
                        value = itemID,
                        enabled = false,
                        readOnly = true,
                        label = { Text(text = "ID") },
                        singleLine = true,
                        onValueChange = {  }
                    )

                    //--- Item Title Text Field
                    var textTitle by remember { mutableStateOf(TextFieldValue(itemTitle)) }
                    OutlinedTextField(
                        value = textTitle,
                        label = { Text(text = "Title") },
                        singleLine = true,
                        onValueChange = {
                            textTitle = it
                        }
                    )
                    itemTitle = textTitle.text

                    //--- Create Updated Item from Parts
                    myItem = Item(
                        id = itemID.toInt(),
                        title = itemTitle
                    )

                    //--- Update Existing Item Button
                    Button(
                        onClick = {
                            navController.navigate(route = NavRoutes.ItemListScreen.route) {
                                popUpTo(NavRoutes.ItemListScreen.route)
                            }
                            viewModel.updateItem(myItem)
                        }) {
                        Text(
                            text = "Submit",
                            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.background),
                        )
                    }    // End Update Existing Item Button

                    Spacer(Modifier.height(10.dp))

                    //--- Delete this Item Button
                    Button(
                        onClick = {
                            navController.navigate(route = NavRoutes.ItemListScreen.route) {
                                popUpTo(NavRoutes.ItemListScreen.route)
                            }
                            viewModel.deleteItem(myItem)
                        }) {
                        Text(
                            text = "Delete this Item",
                            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.background),
                        )
                    }    // End Update Existing Item Button

                }   // End Column

            }   // End If Not Null

        }
        //---------------------------------------------------------
        // Else
        //---------------------------------------------------------
        else -> {
            // Nothing Else To Do
        }
    }   // End When

}   	// End Item Details Screen
