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
    val id: Int,
    var title: String
)

//------------------------------------------------------------------
// Main View Model
//------------------------------------------------------------------
class MainViewModel() {

    //------------------------------------------------------
    // Items Data
    //------------------------------------------------------
    var allItems: List<Item>
    var specificItem: Item

    //------------------------------------------------------
    // Initialize View Model
    //------------------------------------------------------
    init {

        val item01 = Item(id = 1, title = "First Item")
        val item02 = Item(id = 2, title = "Second Item")
        val item03 = Item(id = 3, title = "Third Item")

        allItems = mutableListOf(item01, item02, item03)
        specificItem = Item(id = 0, title = "Initial Item")

    }   // End Initializer

    //------------------------------------------------------
    // Item Methods
    //------------------------------------------------------
    fun insertItem(item: Item) {
        println("**** insertItem($item)")
        allItems = allItems + item
    }

    fun updateItem(item: Item) {
        println("**** updateItem($item)")
//        vids?.find { it.id == 2 }?.iLike = true
        // From: https://stackoverflow.com/questions/54797411/change-a-value-in-mutable-list-in-kotlin
        allItems.find { it.id == item.id }?.title = item.title
    }

    fun getItemByID(id: Int) {
        println("**** getItemByID(id = ${id - 1})")
        specificItem = allItems[id - 1]
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
                // A surface container using the 'background' color from the theme
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
@Composable
fun ItemListScreen(
    navController: NavHostController,
    viewModel: MainViewModel
) {

    Column {

        //------------------------------------------------------------------
        // Title
        //------------------------------------------------------------------
        Text(
            text = "Item List V1",
            modifier = Modifier.padding(start = 10.dp),
            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )

        //------------------------------------------------------------------
        // Add New Item Button
        //------------------------------------------------------------------
        Button(onClick = {
            navController.navigate(
                route = NavRoutes.ItemDetailScreen.route
                        + "/" + "0"
                        + "/" + "CREATE"
            )
        }) {
            Text(
                text = "Add New Item",
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onBackground)
            )
        }

        Divider()

        //------------------------------------------------------------------
        // List All Items
        //------------------------------------------------------------------
        viewModel.allItems.forEach {item ->
            ClickableText(
                text = AnnotatedString("Item: ${item.title}"),
                modifier = Modifier.padding(start = 10.dp),
                style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                onClick = {
                    viewModel.getItemByID(item.id)
                    println("**** NavRoutes.ItemDetailScreen id = $item.id, crud = UPDATE")
                    navController.navigate(
                        route = NavRoutes.ItemDetailScreen.route
                                + "/" + item.id
                                + "/" + "UPDATE"
                    )
                }	// End On Click
            )	// End Clickable Text
            Spacer(modifier = Modifier.height(5.dp))
            Divider()
            Spacer(modifier = Modifier.height(5.dp))
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

    val itemID: Int
    var itemTitle: String
    var myItem: Item

    when (crud) {
        //---------------------------------------------------------
        // Create New Item
        //---------------------------------------------------------
        "CREATE" -> {
            println("**** ItemDetailScreen(id = $id, crud = CREATE)")

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
                        modifier = Modifier.padding(start = 10.dp),
                        style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onBackground),
                    )
                }    // End Add New Item Button

            }    // End Create New Item Column

        }
        //---------------------------------------------------------
        // Update Existing Item
        //---------------------------------------------------------
        "UPDATE" -> {
            println("**** ItemDetailScreen(id = $id, crud = READ or UPDATE)")

            if (id != null) {
                viewModel.getItemByID(id)

                myItem = viewModel.specificItem

                itemID = myItem.id
                itemTitle = myItem.title

                println("**** Current Details are: $itemID | $itemTitle |")

                Column {

                    //--- Item ID Text Field
                    Text(text = "ID (Read Only) = ${itemID.toString()}")
//                    OutlinedTextField(
//                        value = itemID,
//                        enabled = false,
//                        readOnly = true,
//                        label = "ID",
//                        singleLine = true,
//                        onValueChange = { }
//                    )

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
                        id = itemID,
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
                            modifier = Modifier.padding(start = 10.dp),
                            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onBackground),
                        )
                    }    // End Update Existing Item Button

                }   // End Column

            }   // End If Not Null

        }
        //---------------------------------------------------------
        // Else
        //---------------------------------------------------------
        else -> {
            println("**** ItemDetailScreen(Else)")
        }
    }   // End When

}   	// End Item Details Screen
