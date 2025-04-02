package org.aleks616.shrendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shrendar.composeapp.generated.resources.Res
import shrendar.composeapp.generated.resources.compose_multiplatform



val networkClient=NetworkClient()
suspend fun fetchRanks():List<Ranks> =networkClient.fetchRanks()

@Composable
@Preview
fun App(){
    val width=getScreenWidth()
    var ranks by remember{mutableStateOf(emptyList<Ranks>())}
    var language by remember{mutableStateOf("en")}
    val screenWidth by remember{mutableStateOf(width)}

    LaunchedEffect(Unit){
        ranks=fetchRanks()
    }
    LaunchedEffect(screenWidth){}

    var showContent by remember{mutableStateOf(false)}

    var searchValue by remember{mutableStateOf("")}


    AppTheme{
        Box(modifier=Modifier.fillMaxSize()){
            Column(modifier=Modifier.fillMaxSize()){
                /**header**/
                Row(modifier=Modifier.fillMaxWidth().background(color=MaterialTheme.colors.primaryVariant).heightIn(max=60.dp,min=60.dp),
                    horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically){
                    Box(modifier=Modifier/*.scale(1f)*/.fillMaxHeight().align(Alignment.CenterVertically)){
                        Row(modifier=Modifier.fillMaxHeight(),verticalAlignment=Alignment.CenterVertically){
                            //Spacer(modifier=Modifier.size(12.dp))
                            Icon(imageVector=Icons.Default.Place, contentDescription="",
                                modifier=Modifier/*.scale(2f)*/.size(40.dp)/*.padding(horizontal=12.dp,vertical=6.dp).align(Alignment.Top)*/)
                            //todo: actual logo
                            Text("SHRENDAR", textAlign=TextAlign.Center)
                        }
                    }
                    Box(modifier=Modifier./*scale(1f).*/fillMaxHeight().align(Alignment.CenterVertically)){
                        Row(modifier=Modifier./*scale(1f).*/fillMaxHeight(),verticalAlignment=Alignment.CenterVertically){
                            FixedTextField(searchValue,{searchValue=it},"search",
                                modifier=Modifier.padding(end=12.dp),
                                trailingIcon={IconButton(
                                        onClick={
                                            //todo: search
                                        }){
                                        Icon(modifier=Modifier.size(40.dp),imageVector=Icons.Rounded.Search,
                                            contentDescription=null,tint=colorSecondary())
                                    }
                                })
                            Icon(contentDescription=null,imageVector=Icons.Default.AccountCircle,
                                modifier=Modifier.fillMaxHeight().scale(2f).padding(horizontal=4.dp))
                            Spacer(modifier=Modifier.size(14.dp))
                        }
                    }
                }
                /**content**/
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).weight(1f),horizontalAlignment=Alignment.CenterHorizontally){
                    BetterButton(onClick={showContent=!showContent}){
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
                    }
                    ranks.forEach{
                        BetterText(text=it.id.toString()+" "+it.name+" "+it.minXp,fontSize="S")
                    }
                    BetterButton(onClick={language=if(language=="en") "pl" else "en"}){Text("language")}
                    LoginScreen(language)
                }
            }
        }
    }
}
/**
 * @param locale - language code
 * @param key - [StringLocale] key, eg. sc.NAME
 * **/
fun getTranslation(locale:String,key:String):String{
    val sc=StringLocale
    val st=sc.translations
    return st[key]?.get(locale)?:""
}


/**
 * @param value value for [FixedTextField]
 * @param onValueChange for [FixedTextField], put {value=it}
 * @param language - language code e.g. "en" "pl", "en" on default
 * @param fieldTitle - big text displayed above the textField
 * @param fieldPlaceholderText - placeholder for the textField
 * @param fieldDescription - optional, smaller, text bellow fieldTitle
 * @see FixedTextField
 * @see TextField
 * @see CenteredText
 * **/
@Composable
fun TextFieldPlus(
    value:String,
    onValueChange:(String)->Unit,
    language:String="en",
    fieldTitle:String,
    fieldPlaceholderText:String,
    fieldDescription:String="",
){
    CenteredText(text=getTranslation(language,fieldTitle),fontSize=24.sp,
        modifier=if(fieldDescription!="") {Modifier.padding(horizontal=6.dp,vertical=0.dp)} else {Modifier.padding(horizontal=6.dp,vertical=4.dp)})
    if(fieldDescription!=""){
        CenteredText(text=getTranslation(language,fieldDescription),fontSize=18.sp,
            modifier=Modifier.padding(bottom=4.dp))
    }
    FixedTextField(
        value=value,
        onValueChange=onValueChange,
        placeholderText=getTranslation(language,fieldPlaceholderText)
    )

}

@Composable
@Preview
fun LoginScreen(language:String="en"){
    var enteredLogin by remember{mutableStateOf("")}
    var enteredDisplayName by remember{mutableStateOf("")}
    var enteredEmail by remember{mutableStateOf("")}
    var enteredPassword by remember{mutableStateOf("")}
    var enteredConfirmPassword by remember{mutableStateOf("")}

    val sc=StringLocale

    var errorText by remember{mutableStateOf("")}
    var showPasswordSafetyMessage by remember{mutableStateOf(false)}

    LaunchedEffect(errorText){}


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

                Button(
                    onClick={
                        errorText=if(enteredLogin.isEmpty()) getTranslation(language,sc.ERROR_LOGIN_EMPTY)
                        else if(!isEmailValid(enteredEmail)) getTranslation(language,sc.ERROR_EMAIL_INCORRECT)
                        else if(enteredPassword!=enteredConfirmPassword) getTranslation(language,sc.ERROR_PASSWORD_NOT_MATCH)
                        else if(!isPasswordSafe(enteredPassword)) getTranslation(language,sc.ERROR_PASSWORD_NOT_SAFE)
                        else ""

                        showPasswordSafetyMessage=!isPasswordSafe(enteredPassword)

                        //backend todo: check if login already exists, send password as charsequence
                    }
                ){
                    CenteredText("Create account")
                }
            }
        }
    }
}

fun isPasswordSafe(password:String):Boolean{
    if(!(8..32).contains(password.length)) return false

    var upper=false
    var lower=false
    var digit=false
    var symbol=false
    val symbolsSet=setOf('!','@','#','$','%','^','&','*','(',')','-','_','+','=','~','`','<','>',',','.','?','/','|','[',']','{','}',':',';')

    for(ch in password){
        if(ch.isUpperCase()) upper=true
        else if(ch.isLowerCase()) lower=true
        else if(ch.isDigit()) digit=true
        else if(ch in symbolsSet) symbol=true
        if(upper&&lower&&digit&&symbol) return true
    }
    return upper&&lower&&digit&&symbol
}

fun isEmailValid(email:String):Boolean{
    if (email.isBlank()) return false
    val regex="^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)*[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:IPv6:[a-f0-9:]+|(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9]))])$"

    return Regex(regex).matches(email)
}