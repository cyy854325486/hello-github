package com.example.hellogithub

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hellogithub.adapter.RepositoryAdapter
import com.example.hellogithub.viewmodel.RepositoryViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.hellogithub.data.GitHubRepo
import com.example.hellogithub.data.Repository
import com.example.hellogithub.utils.GitHubConfig
import com.example.hellogithub.utils.GitHubConfig.ACCESS_TOKEN_URL
import com.example.hellogithub.utils.GitHubConfig.AUTH_URL
import com.example.hellogithub.utils.GitHubConfig.CLIENT_ID
import com.example.hellogithub.utils.GitHubConfig.CLIENT_SECRET
import com.example.hellogithub.utils.GitHubConfig.RC_AUTH
import com.example.hellogithub.utils.LanguageUtils
import com.example.hellogithub.utils.ThemeUtils
import com.example.hellogithub.viewmodel.ProfileViewModel
import com.example.hellogithub.viewmodel.ProfileViewModelFactory
import com.example.hellogithub.viewmodel.SettingsViewModel
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(dataStore)
    }
    private val repositoryViewModel: RepositoryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsViewModel.language.observe(this) { language ->
            LanguageUtils.applyLanguage(language, this)
        }
        ThemeUtils.applySavedTheme(this)
        setContent {
            MainScreen()
        }
//        settingsViewModel.initLanguage(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_AUTH) {
            val response = AuthorizationResponse.fromIntent(data!!)
            val ex = AuthorizationException.fromIntent(data)

            if (response != null) {
                val code = response.authorizationCode
                Log.d("OAuth", "Authorization code: $code")

                code?.let {
                    profileViewModel.getAccessToken(CLIENT_ID, CLIENT_SECRET, it)
                }
            } else {
                Log.e("OAuth", "Authorization failed: $ex")
            }
        }
    }


    @Composable
    @Preview
    fun MainScreen() {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = { BottomNavBar(navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                Modifier.padding(innerPadding)
            ) {
                composable("home") { GitHubView(repositoryViewModel) }
                composable("profile") { ProfileView(profileViewModel) }
                composable("settings") { SettingsView(settingsViewModel) }
            }
        }
    }

    @Composable
    fun BottomNavBar(navController: NavHostController) {
        BottomNavigation {
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                label = { Text(getString(R.string.tab_home)) },
                selected = false, // You can use `currentDestination` to determine this
                onClick = { navController.navigate("home") }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                label = { Text(getString(R.string.tab_profile)) },
                selected = false, // You can use `currentDestination` to determine this
                onClick = { navController.navigate("profile") }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                label = { Text(getString(R.string.tab_settings)) },
                selected = false, // You can use `currentDestination` to determine this
                onClick = { navController.navigate("settings") }
            )
        }
    }

    @Composable
    @Preview
    fun SettingsView(viewModel: SettingsViewModel = viewModel()) {
        val currentLanguage by viewModel.language.observeAsState(initial = "en")
        val currentTheme by viewModel.theme.observeAsState(initial = "light")

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getString(R.string.page_settings_name)) },
                    actions = {
                        // Additional action buttons can be added here if needed
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // Language selection
                Text(
                    text = getString(R.string.item_title_language),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LanguageSelection(currentLanguage) { selectedLanguage ->
                    viewModel.applyLanguage(selectedLanguage, this@MainActivity)
                }

                // Theme selection
                Text(
                    text = getString(R.string.item_title_theme),
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                ThemeSelection(currentTheme) { selectedTheme ->
                    viewModel.applyTheme(selectedTheme, this@MainActivity)
                }
            }
        }
    }

    @Composable
    fun ThemeSelection(currentTheme: String, onThemeChange: (String) -> Unit) {
        val themes = listOf("Light" to "light", "Dark" to "dark")

        // Display each theme option
        themes.forEach { (themeName, themeCode) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentTheme == themeCode,
                    onClick = { onThemeChange(themeCode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = themeName)
            }
        }
    }


    @Composable
    fun LanguageSelection(currentLanguage: String, onLanguageChange: (String) -> Unit) {
        val languages = listOf("English" to "en", "中文" to "zh")

        // Display each language option
        languages.forEach { (languageName, languageCode) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentLanguage == languageCode,
                    onClick = { onLanguageChange(languageCode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = languageName)
            }
        }
    }


    @Composable
    fun ProfileView(viewModel: ProfileViewModel) {
        val isLoggedIn by viewModel.isLoggedIn.observeAsState(initial = false)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(getString(R.string.page_profile_name)) },
                    contentColor = Color.White,
                    actions = {
                        if (isLoggedIn) {
                            IconButton(onClick = { viewModel.logout() }) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                // Display different UI based on login status
                if (isLoggedIn) {
                    UserProfile(viewModel)
                } else {
                    LoginPanel { startAuthProcess() }
                }
            }
        }
    }

    @Composable
    fun LoginPanel(onClick: () -> Unit) {
        // Login button UI when the user is not logged in
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onClick,
            ) {
                Text(getString(R.string.btn_login), color = Color.White)
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun UserProfile(viewModel: ProfileViewModel) {
        // UI for displaying user profile and repositories after login
        val user by viewModel.user.observeAsState()
        val repos by viewModel.repos.observeAsState(initial = emptyList())
        val isLoading by viewModel.isLoading.observeAsState(initial = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                model = user?.avatar_url,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${user?.login}",
                style = MaterialTheme.typography.h6,
                color = Color(0xFF24292E)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(repos) { repo ->
                        RepoUserItem(repo)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun RepoUserItem(repo: GitHubRepo) {
        // Card style layout for repository item
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = 1.dp,
            onClick = {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.html_url)))
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0366D6)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "URL: ${repo.html_url}",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            }
        }
    }

    private fun startAuthProcess() {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(AUTH_URL),
            Uri.parse(ACCESS_TOKEN_URL)
        )

        val redirectUri = Uri.parse(GitHubConfig.REDIRECT_URI)

        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            AuthorizationRequest.CODE_CHALLENGE_METHOD_PLAIN,
            redirectUri
        ).build()

        val authService = AuthorizationService(applicationContext)
        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, RC_AUTH)
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun GitHubView(viewModel: RepositoryViewModel = viewModel()) {
        val repos by viewModel.repos.observeAsState(initial = emptyList())
        val isLoading by viewModel.isLoading.observeAsState(initial = false)
        var searchText by remember { mutableStateOf(TextFieldValue("")) }
        var selectedLanguage by remember { mutableStateOf("") }
        var selectedStars by remember { mutableStateOf(0) }

        // State to manage dropdown menu visibility
        var isLanguageDropdownExpanded by remember { mutableStateOf(false) }
        var isStarsDropdownExpanded by remember { mutableStateOf(false) }

        // Sample data for dropdown options
        val languages = listOf("Java", "Kotlin", "Python", "JavaScript")
        val starOptions = listOf(1, 10, 50, 100)

        Column(modifier = Modifier.fillMaxSize()) {

            SearchBar(
                searchText = searchText,
                onSearchTextChanged = { searchText = it },
                onSearchClicked = { clearedText ->
                    viewModel.searchRepositories(searchText.text, selectedLanguage, selectedStars)
                    searchText = clearedText
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp), // Consistent padding with other components
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between the dropdowns
            ) {
                // Language Dropdown
                ExposedDropdownMenuBox(
                    expanded = isLanguageDropdownExpanded,
                    onExpandedChange = { isLanguageDropdownExpanded = !isLanguageDropdownExpanded }
                ) {
                    TextField(
                        value = selectedLanguage,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(getString(R.string.select_title_language)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageDropdownExpanded)
                        },
                        modifier = Modifier
                            .width(200.dp)
                            .clickable { isLanguageDropdownExpanded = true }
                    )
                    ExposedDropdownMenu(
                        expanded = isLanguageDropdownExpanded,
                        onDismissRequest = { isLanguageDropdownExpanded = false }
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedLanguage = language
                                    isLanguageDropdownExpanded = false // Close dropdown on selection
                                }
                            ) {
                                Text(text = language)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stars Dropdown
                ExposedDropdownMenuBox(
                    expanded = isStarsDropdownExpanded,
                    onExpandedChange = { isStarsDropdownExpanded = !isStarsDropdownExpanded }
                ) {
                    TextField(
                        value = if (selectedStars > 0) "$selectedStars stars" else "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(getString(R.string.select_title_stars)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isStarsDropdownExpanded)
                        },
                        modifier = Modifier
                            .width(200.dp)
                            .clickable {
                                isStarsDropdownExpanded = true
                            }
                    )
                    ExposedDropdownMenu(
                        expanded = isStarsDropdownExpanded,
                        onDismissRequest = { isStarsDropdownExpanded = false }
                    ) {
                        starOptions.forEach { stars ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedStars = stars
                                    isStarsDropdownExpanded = false // Close dropdown on selection
                                }
                            ) {
                                Text(text = "$stars stars")
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                }
            } else {
                RepoList(repos = repos)
            }
        }
    }

    @Composable
    fun SearchBar(
        searchText: TextFieldValue,
        onSearchTextChanged: (TextFieldValue) -> Unit,
        onSearchClicked: (TextFieldValue) -> Unit
    ) {
        val keyboardController = LocalSoftwareKeyboardController.current
        Row(modifier = Modifier
            .padding(16.dp)
            .height(50.dp)) {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChanged,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(0.dp),
                placeholder = {
                    Text(
                        text = getString(R.string.place_holder_search),
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal
                        )
                    )
                              },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                ),
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    placeholderColor = Color.Gray,
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black,
                ),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onSearchClicked(TextFieldValue(""))
                    keyboardController?.hide()
                },
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(text = getString(R.string.btn_search))
            }
        }
    }

    @Composable
    fun RepoList(repos: List<Repository>) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(repos) { repo ->
                RepoItem(repo)
            }
        }
    }

    @Composable
    fun SubmitIssueDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Title of the dialog
                    Text(
                        text = getString(R.string.dialog_banner_title),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Title input with light border
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp), // Padding inside the border
                        decorationBox = { innerTextField ->
                            if (title.isEmpty()) {
                                Text(getString(R.string.dialog_title), color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description input with light border
                    BasicTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp), // Padding inside the border
                        decorationBox = { innerTextField ->
                            if (description.isEmpty()) {
                                Text(getString(R.string.dialog_description), color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onDismiss) {
                            Text(getString(R.string.btn_cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSubmit(title, description) },
                            enabled = title.isNotBlank() && description.isNotBlank()
                        ) {
                            Text(getString(R.string.btn_submit))
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterialApi::class)
    @Composable
    fun RepoItem(repo: Repository) {
        val isLoggedIn by profileViewModel.isLoggedIn.observeAsState(initial = false)
        var showDialog by remember { mutableStateOf(false) }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp),
            elevation = 1.dp,
            onClick = {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repo.html_url)))
            }
        ) {
            Row(modifier = Modifier.padding(8.dp)) {
                GlideImage(
                    model = repo.owner.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),

                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(5f)
                ) {
                    Text(text = repo.name, style = MaterialTheme.typography.h6)
                    Text(
                        text = repo.description ?: "No description",
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (isLoggedIn) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Logout",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }
        }

        if (showDialog) {
            SubmitIssueDialog(
                onDismiss = { showDialog = false },
                onSubmit = { title, description ->
                    profileViewModel.submitIssue(repo, title, description)
                    showDialog = false
                }
            )
        }
    }
}