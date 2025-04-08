package org.aleks616.shrendar
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.sharp.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.aleks616.shrendar.Utils.getTranslation
import org.aleks616.shrendar.Utils.isEmailValid
import org.aleks616.shrendar.Utils.isPasswordSafe
import org.jetbrains.compose.ui.tooling.preview.Preview


val networkClient=NetworkClient()
suspend fun fetchRanks():List<Ranks> =networkClient.fetchRanks()
suspend fun fetchUsers():List<UsersDto> =networkClient.fetchUsers()
suspend fun doesLoginExist(login:String):Boolean=networkClient.doesLoginExist(login)
suspend fun doesAccountWithEmailExist(email:String):Boolean=networkClient.doesEmailExist(email)
suspend fun isPasswordCorrect(email:String?=null,login:String?=null,password:CharArray):Boolean =networkClient.isPasswordCorrect(email,login,password)
suspend fun sendRegister(login:String,displayName:String,email:String,password:CharArray)=networkClient.sendRegister(login,displayName,email,password)

val h0=36.sp
val h1=32.sp
val h2=28.sp
val h3=24.sp
val h4=20.sp
val h5=16.sp

@Composable
@Preview
fun App(){
    val width=getScreenWidth()
    var ranks by remember{mutableStateOf(emptyList<Ranks>())}
    var users by remember{mutableStateOf(emptyList<UsersDto>())}
    //var language by remember{mutableStateOf(getLanguage().code)}

    //LaunchedEffect(Unit){language=getLanguage().code}

    val screenWidth by remember{mutableStateOf(width)}
    var dropDownAccountVisible by remember{mutableStateOf(false)}

    LaunchedEffect(dropDownAccountVisible){

    }
    LaunchedEffect(Unit){
        ranks=fetchRanks()
        users=fetchUsers()
    }
    LaunchedEffect(screenWidth){}

    //var showContent by remember{mutableStateOf(false)}

    var searchValue by remember{mutableStateOf("")}

    val narrow=width<450.dp

    AppTheme{
        Box(modifier=Modifier.fillMaxSize()){
            if(dropDownAccountVisible){
                Row(modifier=Modifier.fillMaxWidth().zIndex(2f).offset(y=60.dp),horizontalArrangement=Arrangement.End){
                    /**drop down menus**/
                    Column(Modifier.background(color=MaterialTheme.colors.primaryVariant)){
                        CenteredText("test1",Modifier.padding(horizontal=6.dp))
                        CenteredText("test2",Modifier.padding(horizontal=6.dp))
                        CenteredText("test3",Modifier.padding(horizontal=6.dp))
                        CenteredText("test4",Modifier.padding(horizontal=6.dp))
                    }
                }
            }
            Column(modifier=Modifier.fillMaxSize()){
                /**header**/
                Row(modifier=Modifier.fillMaxWidth().background(color=MaterialTheme.colors.primaryVariant).heightIn(max=60.dp,min=60.dp),
                    horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){
                    Box(modifier=Modifier.fillMaxHeight().align(Alignment.CenterVertically)){
                        Row(modifier=Modifier.fillMaxHeight(),verticalAlignment=Alignment.CenterVertically){ /**left header**/
                            Icon(imageVector=Icons.Default.Place, contentDescription="",
                                modifier=Modifier.size(40.dp))
                            //todo: actual logo
                            Text("SHRENDAR", textAlign=TextAlign.Center)
                        }
                    }
                    Box(modifier=Modifier.fillMaxHeight().align(Alignment.CenterVertically)){  /**right header**/
                        Row(modifier=Modifier.fillMaxHeight(),verticalAlignment=Alignment.CenterVertically){
                            FixedTextField(searchValue,{searchValue=it}, placeholderText=if(!narrow) "search" else "",
                                modifier=if(narrow) Modifier.width(120.dp) else Modifier
                                    .padding(end=12.dp),
                                trailingIcon={IconButton(
                                        onClick={
                                            //todo: search
                                        }){
                                        Icon(modifier=Modifier.size(40.dp),imageVector=Icons.Rounded.Search,
                                            contentDescription=null,tint=colorSecondary())
                                    }
                                })
                            val scale=if(narrow) 1.4f else 2f
                            Icon(contentDescription=null,imageVector=Icons.Sharp.ThumbUp,
                                modifier=Modifier.fillMaxHeight().scale(scale).padding(horizontal=16.dp).offset(x=(-4).dp))

                            IconButton(onClick={dropDownAccountVisible=!dropDownAccountVisible}){
                                Icon(contentDescription=null,imageVector=Icons.Default.AccountCircle,
                                    modifier=Modifier.fillMaxHeight().scale(scale).padding(horizontal=4.dp))
                            }
                            Spacer(modifier=Modifier.size(14.dp))
                        }
                    }
                }

                /**content**/
                Row(Modifier.fillMaxWidth()){
                    Column(Modifier.weight(0.2f).background(color=MaterialTheme.colors.primaryVariant).fillMaxSize()){
                        CenteredText("Today",fontSize=h3)
                        //loop for suggested events today
                    }
                    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).weight(1f),horizontalAlignment=Alignment.CenterHorizontally){

                    /*BetterButton(onClick={showContent=!showContent}){
                        Text("Click me!")
                    }
                    AnimatedVisibility(showContent){
                        val greeting=remember{Greeting().greet()}
                        Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){
                            Image(painterResource(Res.drawable.compose_multiplatform),null)
                            BetterText("Compose: $greeting",fontSize="M")
                            //CenteredText(Greeting().getPlatform())
                            CenteredText(getScreenWidth().toString())
                        }
                    }*/
                    ranks.forEach{
                        BetterText(text=it.id.toString()+" "+it.name+" "+it.minXp,fontSize="S")
                    }
                    CenteredText(users.size.toString(),fontSize=30.sp)
                    users.forEach{
                        BetterText(text=it.id.toString()+" "+it.login+" "+it.createdAt+" "+it.ranks?.name,fontSize="S")
                    }
                    BetterButton(
                        onClick={
                            setLanguage("en") //works only after hard reload! //todo: move the button+don't hardcode
                        }
                    ){
                        Text("language")
                    }

                    //RegisterScreen()
                    //LoginScreen()
                }

                    Column(Modifier.weight(0.2f).background(color=MaterialTheme.colors.primaryVariant).fillMaxSize()){
                        CenteredText("test right")
                    }
                }
            }


        }
    }
}

@Composable
@Preview
fun RegisterScreen(language:String=getLanguage().code){
    var enteredLogin by remember{mutableStateOf("")}
    var enteredDisplayName by remember{mutableStateOf("")}
    var enteredEmail by remember{mutableStateOf("")}
    var enteredPassword by remember{mutableStateOf("")}
    var enteredConfirmPassword by remember{mutableStateOf("")}

    val sc=StringLocale

    var errorText by remember{mutableStateOf("")}
    var showPasswordSafetyMessage by remember{mutableStateOf(false)}

    LaunchedEffect(errorText){}

    var loginExists:Boolean? by mutableStateOf(null)
    var accountWithEmailExists:Boolean? by mutableStateOf(null)

    LaunchedEffect(enteredLogin){
        loginExists=doesLoginExist(enteredLogin)
    }
    LaunchedEffect(enteredEmail){
        accountWithEmailExists=doesAccountWithEmailExist(enteredEmail)
        loginExists=doesLoginExist(enteredLogin)
    }

    AppTheme{
       Box(modifier=Modifier.fillMaxSize()){
            Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment=Alignment.CenterHorizontally){

                TextFieldPlus(enteredLogin,{enteredLogin=it},language,
                    sc.CREATE_LOGIN_TITLE,sc.CREATE_LOGIN_PLACEHOLDER,sc.CREATE_LOGIN_DESCRIPTION)

                TextFieldPlus(enteredDisplayName,{enteredDisplayName=it},language,
                    sc.DISPLAY_NAME_TITLE,sc.DISPLAY_NAME_PLACEHOLDER,sc.DISPLAY_NAME_DESCRIPTION)

                TextFieldPlus(enteredEmail,{enteredEmail=it},language,
                    sc.ENTER_EMAIL_TITLE,sc.ENTER_EMAIL_PLACEHOLDER)

                TextFieldPlus(enteredPassword,{enteredPassword=it},language,
                    sc.CREATE_PASSWORD_TITLE,sc.PASSWORD_PLACEHOLDER)

                TextFieldPlus(enteredConfirmPassword,{enteredConfirmPassword=it},language,
                    sc.CONFIRM_PASSWORD_TITLE,sc.PASSWORD_PLACEHOLDER)

                Text(color=MaterialTheme.colors.onError,text=errorText)
                if(showPasswordSafetyMessage){CenteredText(color=MaterialTheme.colors.onError,text=getTranslation(language,sc.PASSWORD_SAFE_MESSAGE))}

                Text(loginExists.toString())
                Text(accountWithEmailExists.toString())

                Button(
                    onClick={
                        errorText=if(enteredLogin.isEmpty()) getTranslation(language,sc.ERROR_LOGIN_EMPTY)
                        else if(!isEmailValid(enteredEmail)) getTranslation(language,sc.ERROR_EMAIL_INCORRECT)
                        else if(enteredPassword!=enteredConfirmPassword) getTranslation(language,sc.ERROR_PASSWORD_NOT_MATCH)
                        else if(!isPasswordSafe(enteredPassword)) getTranslation(language,sc.ERROR_PASSWORD_NOT_SAFE)
                        else if(loginExists==true) getTranslation(language,sc.ERROR_LOGIN_ALREADY_EXISTS)
                        else if(accountWithEmailExists==true) getTranslation(language,sc.ERROR_EMAIL_ALREADY_EXISTS) //TODO: CHANGE TO !=FALSE AND RESOLVE THE NULL
                        else ""
                        showPasswordSafetyMessage=!isPasswordSafe(enteredPassword)


                        if(errorText.isEmpty()){
                            val passwordCharArray=enteredPassword.toCharArray()
                            CoroutineScope(Dispatchers.Default).launch{
                                sendRegister(enteredLogin,enteredDisplayName,enteredEmail,passwordCharArray)
                            }
                            //passwordCharArray.fill('\u0000')
                        }
                    }
                ){
                    CenteredText(getTranslation(language,sc.CREATE_ACCOUNT))
                }
            }
        }
    }
}

@Composable
@Preview
fun LoginScreen(language:String=getLanguage().code){
    var enteredLogin by remember{mutableStateOf("")}
    var enteredPassword by remember{mutableStateOf("")}
    val sc=StringLocale

    var errorText by remember{mutableStateOf("test")}
    LaunchedEffect(errorText){

    }


    var isEmail by remember{mutableStateOf(false)}
    LaunchedEffect(isEmail){

    }

    var accountExists:Boolean? by mutableStateOf(null)
    LaunchedEffect(enteredLogin){
        accountExists=if(isEmail) doesAccountWithEmailExist(enteredLogin)
        else doesLoginExist(enteredLogin)
    }

    var isPasswordCorrect:Boolean? by remember{mutableStateOf(null)}
    LaunchedEffect(isPasswordCorrect){
        if(enteredPassword!=""){
            isPasswordCorrect=if(isEmailValid(enteredLogin)) isPasswordCorrect(email=enteredLogin, password=enteredPassword.toCharArray())
            else isPasswordCorrect(login=enteredLogin,password=enteredPassword.toCharArray())
        }

    }


    AppTheme{
        Box(modifier=Modifier.fillMaxSize()){
            Column(modifier=Modifier.fillMaxWidth(), horizontalAlignment=Alignment.CenterHorizontally){
                TextFieldPlus(enteredLogin,{enteredLogin=it},language,
                    getTranslation(language,sc.ENTER_LOGIN_TITLE),sc.ENTER_LOGIN_PLACEHOLDER)

                TextFieldPlus(enteredPassword,{enteredPassword=it},language,
                    getTranslation(language,sc.ENTER_PASSWORD_TITLE),sc.ENTER_PASSWORD_PLACEHOLDER)

                Button(onClick={
                    if(isEmailValid(enteredLogin)) isEmail=true

                    val passwordCharArray=enteredPassword.toCharArray()
                    CoroutineScope(Dispatchers.Default).launch{
                        if(isEmail){
                            if(doesAccountWithEmailExist(enteredLogin))
                                isPasswordCorrect=isPasswordCorrect(email=enteredLogin,password=passwordCharArray)
                            else
                                errorText=getTranslation(language,sc.ERROR_EMAIL_DOES_NOT_EXIST)
                        }
                        else{
                            if(doesLoginExist(enteredLogin))
                                isPasswordCorrect=isPasswordCorrect(login=enteredLogin,password=passwordCharArray)
                            else
                                errorText=getTranslation(language,sc.ERROR_LOGIN_DOES_NOT_EXIST)
                        }
                    }.invokeOnCompletion{
                        errorText=if(isPasswordCorrect==false) "Password is incorrect" else if(isPasswordCorrect==null) "null" else "good"
                    }
                    //passwordCharArray.fill('\u0000')


                }){
                    CenteredText(text=getTranslation(language,sc.LOG_IN_BUTTON))
                }

                Text(color=MaterialTheme.colors.onError,text=errorText)
            }
        }
    }
}