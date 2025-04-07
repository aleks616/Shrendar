package org.aleks616.shrendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


/**
 * literally centered [Text], everything is copied
 * **/
@Composable
fun CenteredText(
    text:String,
    modifier:Modifier=Modifier,
    fontSize:TextUnit=18.sp,
    color:Color=Color.Unspecified,
    fontStyle:FontStyle?=null,
    fontWeight:FontWeight?=null,
    fontFamily:FontFamily?=null,
    letterSpacing:TextUnit=TextUnit.Unspecified,
    textDecoration:TextDecoration?=null,
    lineHeight:TextUnit=TextUnit.Unspecified,
    overflow:TextOverflow=TextOverflow.Clip,
    softWrap:Boolean=true,
    maxLines:Int=Int.MAX_VALUE,
    minLines:Int=1,
    onTextLayout:((TextLayoutResult)->Unit)?=null,
    style:TextStyle=LocalTextStyle.current
){
    Text(text=text, modifier=modifier.padding(all=6.dp),fontSize=fontSize, textAlign=TextAlign.Center,

        color=color,fontStyle=fontStyle,fontWeight=fontWeight,fontFamily=fontFamily,letterSpacing=letterSpacing,
        textDecoration=textDecoration,lineHeight=lineHeight,overflow=overflow,softWrap=softWrap,maxLines=maxLines,
        minLines=minLines,onTextLayout=onTextLayout,style=style)
}


/**
 * Looks almost like [TextField] except it doesn't have huge content padding.
 * Made with [BasicTextField].
 * Changing cursor color is impossible as of now.
 * @see TextField
 * **/
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FixedTextField(
    value:String,
    onValueChange:(String) -> Unit,
    placeholderText:String,
    modifier:Modifier=Modifier,
    visualTransformation:VisualTransformation=remember{VisualTransformation.None},
    keyboardOptions:KeyboardOptions=KeyboardOptions.Default,
    fontSize:TextUnit=18.sp,
    leadingIcon:@Composable (()->Unit)?=null,
    trailingIcon:@Composable (()->Unit)?=null,
    maxLines:Int=1
){
    val interactionSource=remember {MutableInteractionSource()}

    BasicTextField(
        value=value,
        onValueChange=onValueChange,
        textStyle=TextStyle(fontSize=fontSize,color=colorSecondary()),
        modifier=modifier
            .background(color=Color.Unspecified,shape=RoundedCornerShape(25.dp))
            .height(40.dp),
        visualTransformation=visualTransformation,
        keyboardOptions=keyboardOptions,
        interactionSource=interactionSource,
        singleLine=(maxLines==1),

        decorationBox={innerTextField ->
            TextFieldDefaults.TextFieldDecorationBox(
                value=value,
                innerTextField=innerTextField,
                enabled=true,
                singleLine=(maxLines==1),
                leadingIcon=leadingIcon,
                trailingIcon=trailingIcon,
                visualTransformation=visualTransformation,
                interactionSource=interactionSource,
                placeholder={
                    Text(text=placeholderText,fontSize=fontSize)
                },
                contentPadding=PaddingValues(horizontal=16.dp,vertical=6.dp),
                colors=TextFieldDefaults.textFieldColors(
                    textColor=colorSecondary(),
                    placeholderColor=colorSecondary(),
                    cursorColor=colorSecondary(),
                ),
            )
        }
    )
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
    CenteredText(text=Utils.getTranslation(language,fieldTitle),fontSize=24.sp,
        modifier=if(fieldDescription!="") {Modifier.padding(horizontal=6.dp,vertical=0.dp)} else {Modifier.padding(horizontal=6.dp,vertical=4.dp)})
    if(fieldDescription!=""){
        CenteredText(text=Utils.getTranslation(language,fieldDescription),fontSize=18.sp,
            modifier=Modifier.padding(bottom=4.dp))
    }
    FixedTextField(
        value=value,
        onValueChange=onValueChange,
        placeholderText=Utils.getTranslation(language,fieldPlaceholderText)
    )

}






@Composable
fun VerticalSpacer(size:Dp=25.dp){
    Spacer(modifier=Modifier.padding(vertical=size))

}

@Composable
fun AutoComplete(testosteroneName:String,placeholderText:String,onValueChange:(String)->Unit,versions:List<String>){

    var category by remember{mutableStateOf(testosteroneName)}
    var textFieldSize by remember{mutableStateOf(Size.Zero)}
    var expanded by remember{mutableStateOf(false)}
    val interactionSource=remember{MutableInteractionSource()}

    Column(
        modifier=Modifier
            .padding(vertical=10.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource=interactionSource,
                indication=null,
                onClick={
                    expanded=false
                }
            )
    ){
        Row(
            modifier=Modifier.fillMaxWidth()
        ){
            BetterTextField(
                textFieldModifier=Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {coordinates->
                        textFieldSize=coordinates.size.toSize()
                    },
                value=category,
                onValueChange={value->
                    category=value
                    onValueChange(value)
                    expanded=true
                },
                placeholderText=placeholderText,
                placeholderTextModifier=Modifier.fillMaxWidth(),
                trailingIcon={
                    IconButton(
                        onClick={
                            expanded=!expanded
                            category=""
                        }
                    ){
                        Icon(
                            modifier=Modifier
                                .size(58.dp)
                                .padding(end=10.dp),
                            imageVector=Icons.Rounded.KeyboardArrowDown,
                            contentDescription="",
                            tint=colorSecondary()
                        )
                    }
                }
            )
        }
        AnimatedVisibility(
            visible=expanded,
            enter=fadeIn(animationSpec=tween(durationMillis=150))+expandVertically(),
            exit=fadeOut(animationSpec=tween(durationMillis=150))+shrinkVertically()
        ){
            Card(
                modifier=Modifier
                    .padding(horizontal=15.dp)
                    .fillMaxWidth(),
                shape=RectangleShape
            ){
                LazyColumn(
                    modifier=Modifier
                        .heightIn(max=200.dp)
                        .background(colorPrimary()),
                ){
                    if(category.isNotEmpty()){
                        items(
                            versions.filter{
                                it.lowercase().contains(category.lowercase())
                            }.sorted()
                        ){
                            ItemsCategory(title=it){title->
                                category=title
                                onValueChange(title)
                                expanded=false
                            }
                        }
                    }
                    else{
                        items(
                            versions.sorted()
                        ){
                            ItemsCategory(title=it){title->
                                category=title
                                onValueChange(title)
                                expanded=false
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun ItemsCategory(
    title:String,
    onSelect:(String)->Unit){
    Row(
        modifier=Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable {
                onSelect(title)
            }
    ){
        Text(text=title,fontSize=18.sp,color=colorSecondary())
    }
    Divider(color=colorTertiary())
}



/**
 * @param questionFontSize fontSize for [BetterHeader]
 */
@Composable
fun YesNoRow(
    questionFontSize:String="M",
    onClickYes:()->Unit,
    onClickNo:()->Unit,
    question:String
){
    Column(modifier=Modifier.fillMaxWidth()){
        BetterHeader(
            text=question,
            fontSize=questionFontSize,
            modifier=Modifier
                .fillMaxWidth()
                .padding(vertical=15.dp,horizontal=10.dp)
        )
        Row(
            modifier=Modifier
                .padding(vertical=12.dp,horizontal=12.dp)
                .fillMaxWidth(),
            Arrangement.SpaceAround
        ){
            BetterButton(
                onClick=onClickYes
            ){
                BetterText(
                    text="yes",
                    fontSize=32.sp,
                    modifier=Modifier.padding(vertical=6.dp)
                )
            }
            BetterButton(
                onClick=onClickNo
            ){
                BetterText(
                    text="no",
                    fontSize=32.sp,
                    modifier=Modifier.padding(vertical=6.dp)
                )
            }
        }
    }
}
@Composable
fun CheckboxRow(
    rowModifier:Modifier=Modifier,
    checked:Boolean,
    onCheckedChange:(Boolean)->Unit,
    text:String,
    textModifier:Modifier=Modifier
){
    Row(modifier=rowModifier
        .fillMaxWidth()
        .padding(horizontal=10.dp,vertical=10.dp)){
        BetterCheckbox(
            checked=checked,
            onCheckedChange=onCheckedChange,
        )
        BetterHeader(
            text=text,
            fontSize="MS",
            textAlign=TextAlign.Start,
            modifier=textModifier
                .align(Alignment.CenterVertically)
                .padding(start=8.dp)
        )
    }
}

@Composable
fun BetterCheckbox(
    checked:Boolean=false,
    onCheckedChange:(Boolean)->Unit
){
    Checkbox(
        checked=checked,
        onCheckedChange=onCheckedChange,
        colors=CheckboxDefaults.colors(checkedColor=colorSecondary(),uncheckedColor=colorSecondary(), checkmarkColor=colorTertiary())
    )
}

@Composable
fun BetterButton(
    onClick:()->Unit,
    modifier:Modifier=Modifier,
    enabled:Boolean=true,
    backgroundColor:Color=colorTertiary(),
    shape:androidx.compose.ui.graphics.Shape=RectangleShape,
    content:@Composable RowScope.()->Unit,
){
    val selected=remember {mutableStateOf(false)}
    val scale=animateFloatAsState(if(selected.value) 1.1f else 1f,label="")
    Button(
        onClick={selected.value=false;onClick()},
        modifier=modifier
            .pointerInput(enabled){
                    detectTapGestures(
                        onPress={
                            selected.value=true
                            try{
                                awaitRelease()
                            }
                            finally{
                                selected.value=false
                            }
                        },
                        onTap={}
                    )
            }
            .scale(scale.value),
        enabled=enabled,
        colors=ButtonDefaults.buttonColors(backgroundColor=backgroundColor),
        shape=shape,
        content=content,
        contentPadding=PaddingValues(0.dp),
    )
}

/** use with modifier weight(1f) when in row!**/
@Composable
fun BetterTextField(
    value:String,
    onValueChange:(String)->Unit,
    textFieldModifier:Modifier=Modifier,
    placeholderText:String="",
    placeholderTextModifier:Modifier=Modifier,
    visualTransformation:VisualTransformation=VisualTransformation.None,
    keyboardType:KeyboardType=KeyboardType.Text,
    imeAction:ImeAction=ImeAction.Default,
    trailingIcon:@Composable (()->Unit)?=null,
){
    TextField(
        value=value,
        onValueChange=onValueChange,

        modifier=textFieldModifier
            .padding(start=15.dp,end=10.dp),
        colors=TextFieldDefaults.textFieldColors(
            placeholderColor=colorSecondary(),
            textColor=colorSecondary(),
            focusedIndicatorColor=colorSecondary(),
            unfocusedIndicatorColor=colorSecondary(),
            cursorColor=colorSecondary()
        ),
        textStyle=TextStyle(fontSize=24.sp),
        placeholder={
            BetterText(
                text=placeholderText,
                fontSize=24.sp,
                modifier=placeholderTextModifier.fillMaxWidth(),
                textAlign=TextAlign.Center
            )},
        maxLines=1,
        visualTransformation=visualTransformation,
        keyboardOptions=KeyboardOptions.Default.copy(keyboardType=keyboardType, imeAction=imeAction),
        trailingIcon=trailingIcon,
    )
}

@Composable
fun SimpleNumberTextField(
    value:String,
    onValueChange:(String)->Unit,
    textFieldModifier:Modifier=Modifier,
    placeholderText:String="",
    placeholderTextModifier:Modifier=Modifier,
){
    TextField(
        value=value,
        onValueChange=onValueChange,
        modifier=textFieldModifier
            .width(250.dp)
            .padding(start=20.dp,top=10.dp,bottom=10.dp),
        colors=TextFieldDefaults.textFieldColors(
            placeholderColor=colorSecondary(),
            textColor=colorSecondary(),
            focusedIndicatorColor=colorSecondary(),
            unfocusedIndicatorColor=colorSecondary(),
            cursorColor=colorSecondary()
        ),
        textStyle=TextStyle(fontSize=24.sp),
        placeholder={
            BetterText(
                text=placeholderText,
                fontSize=24.sp,
                modifier=placeholderTextModifier,
                textAlign=TextAlign.Center
            )},
        maxLines=1,
        keyboardOptions=KeyboardOptions.Default.copy(keyboardType=KeyboardType.Number, imeAction=ImeAction.Default)
    )
}

@Composable
fun GoBackButtonRow(
    onClick:()->Unit,
    rowModifier:Modifier=Modifier,
    iconButtonModifier:Modifier=Modifier,
    imageScale:Float=2.5f,
    enabled:Boolean=true
){
    Row(
        modifier=rowModifier.fillMaxWidth(),
        horizontalArrangement=Arrangement.Start
    ){
        IconButton(
            onClick=onClick,
            modifier=iconButtonModifier.padding(8.dp),
            enabled=enabled
        ){
            Icon(
                imageVector=Icons.AutoMirrored.Rounded.ArrowBack,
                tint=colorTertiary(),
                contentDescription=null,
                modifier=Modifier
                    .scale(imageScale)
            )
        }
    }
}

@Composable
fun CheckmarkButtonRow(
    onClick:()->Unit,
    rowModifier:Modifier=Modifier,
    iconButtonModifier:Modifier=Modifier,
    imageScale:Float=3.5f,
    enabled:Boolean=true
){
    Row(
        modifier=rowModifier.fillMaxWidth(),
        horizontalArrangement=Arrangement.End
    ){
        IconButton(
            onClick=onClick,
            modifier=iconButtonModifier.padding(top=30.dp,end=35.dp),
            enabled=enabled
        ){
            Icon(
                imageVector=Icons.Rounded.CheckCircle,
                tint=colorTertiary(),
                contentDescription=null,
                modifier=Modifier
                    .scale(imageScale)
            )
        }
    }
}

/**
 * @param textAlign center by default
 * @param fontSize XL, L, ML, M, MS, S, XS from 40.sp to 16.sp, default - ML(32.sp), you can use lowercase as well
 * @see [sizeToSp]
 * **/
@Composable
fun BetterHeader(
    text:String,
    modifier:Modifier=Modifier.fillMaxWidth(),
    textAlign:TextAlign=TextAlign.Center,
    fontSize:String="ML",
    fontStyle:FontStyle=FontStyle.Normal,
    color:Color=colorSecondary()
){
    BetterText(
        fontSize=sizeToSp(fontSize),
        text=text,
        textAlign=textAlign,
        modifier=modifier.padding(horizontal=5.dp),
        fontStyle=fontStyle,
        color=color
    )
}


/**
 * @param textAlign center by default
 * @param fontSize in sp, default 32
 * **/
@Composable
fun BetterHeader(
    text:String,
    modifier:Modifier=Modifier.fillMaxWidth(),
    textAlign:TextAlign=TextAlign.Center,
    fontSize:TextUnit=32.sp,
    fontStyle:FontStyle=FontStyle.Normal
){
    BetterText(
        fontSize=fontSize,
        text=text,
        textAlign=textAlign,
        modifier=modifier.padding(horizontal=5.dp),
        fontStyle=fontStyle
    )
}


@Composable
fun BetterText(
    text:String,
    modifier:Modifier=Modifier,
    textAlign:TextAlign=TextAlign.Start,
    fontSize:TextUnit=16.sp,
    fontStyle:FontStyle=FontStyle.Normal,
    color:Color=colorSecondary()
){
    Text(
        fontSize=fontSize,
        text=text,
        textAlign=textAlign,
        modifier=modifier,
        letterSpacing=0.sp,
        color=color,
        fontStyle=fontStyle
    )
}

/** @see sizeToSp**/
@Composable
fun BetterText(
    text:String,
    modifier:Modifier=Modifier,
    textAlign:TextAlign=TextAlign.Start,
    fontSize:String="XS",
    fontStyle:FontStyle=FontStyle.Normal,
    color:Color=colorSecondary()
){
    Text(
        fontSize=sizeToSp(fontSize),
        text=text,
        textAlign=textAlign,
        modifier=modifier,
        letterSpacing=0.sp,
        color=color,
        fontStyle=fontStyle
    )
}
/**
 * XL-40, L-36, ML-32, M-28, MS-24, S-20, XS-16
 * **/
fun sizeToSp(fontSize:String):TextUnit{
    return when(fontSize.uppercase()){
        "XL"->40.sp
        "L"->36.sp
        "ML"->32.sp
        "M"->28.sp
        "MS"->24.sp
        "S"->20.sp
        "XS"->16.sp
        else->{32.sp}
    }
}


@Composable
fun ErrorText(
    text:String,
    modifier:Modifier=Modifier
        .fillMaxWidth()
        .padding(top=20.dp,bottom=10.dp)
){
    Text(
        text=text,
        color=Color.Red,
        modifier=modifier,
        textAlign=TextAlign.Center,
        fontSize=28.sp,
        fontStyle=FontStyle.Italic
    )
}

/**
 * @param minValue 0 by default
 * @param maxValue 100 by default
 * @param width width for modifier
 * @param arrowHeight height reserved for each icon button, NOT arrow scale, use to create space between arrows
 * @see BetterTextField
 * **/
@Composable
fun NumberInput(
    arrowTint:Color=colorSecondary(),
    arrowHeight:Dp=26.dp,
    startValue:Int=10,
    minValue:Int=0,
    maxValue:Int=100,
    step:Int=1,
    onValueChange:(String) -> Unit,
    width:Dp=150.dp,
    modifier:Modifier=Modifier,
    arrowScale:Float=1.3f
){
    val startValueStr:String=if((minValue..maxValue).contains(startValue)) startValue.toString() else "0"
    var value by remember {mutableStateOf(startValueStr)}
    BetterTextField(
        value=value,
        onValueChange={value=it},
        textFieldModifier=modifier
            .width(width)
            .fillMaxWidth(),
        imeAction=ImeAction.Done,
        keyboardType=KeyboardType.Number,
        trailingIcon={Column{
            IconButton(
                onClick={
                    if(Utils.isDigitsOnly(value)) if(value.toInt()<=maxValue-step) value=(value.toInt()+step).toString()
                    val newValue=(value.toInt()).toString()
                    onValueChange(newValue)
                },
                modifier=Modifier.height(arrowHeight)
            ){
                Icon(
                    imageVector=Icons.Default.KeyboardArrowUp,
                    contentDescription="Increase",
                    tint=arrowTint,
                    modifier=Modifier.scale(arrowScale)
                )}
            IconButton(
                onClick={
                    if(Utils.isDigitsOnly(value)) if(value.toInt()>=minValue+step) value=(value.toInt()-step).toString()
                    val newValue=(value.toInt()).toString()
                    onValueChange(newValue)
                },
                modifier=Modifier.height(arrowHeight)
            ){
                Icon(
                    imageVector=Icons.Default.KeyboardArrowDown,
                    contentDescription="Decrease",
                    tint=arrowTint,
                    modifier=Modifier.scale(arrowScale)
                )
            }
        }}
    )
}





@Composable
fun DateInputField(
    onDayValueChange:(String)->Unit,
    onMonthValueChange:(String)->Unit,
    onYearValueChange:(String)->Unit
){
    val year=remember{mutableStateOf("")}
    val month=remember{mutableStateOf("")}
    val day=remember{mutableStateOf("")}

    val focusManager=LocalFocusManager.current
    val yearFocusRequester=remember{FocusRequester()}
    val monthFocusRequester=remember{FocusRequester()}
    val dayFocusRequester=remember{FocusRequester()}

    val colors=TextFieldDefaults.outlinedTextFieldColors(
        textColor=colorSecondary(),
        disabledTextColor=colorSecondary(),
        cursorColor=colorSecondary(),
        errorCursorColor=colorSecondary(),
        leadingIconColor=colorSecondary(),
        disabledLeadingIconColor=colorSecondary(),
        errorLeadingIconColor=colorSecondary(),
        trailingIconColor=colorSecondary(),
        disabledTrailingIconColor=colorSecondary(),
        errorTrailingIconColor=colorSecondary(),
        backgroundColor=colorPrimary(),
        focusedLabelColor=colorSecondary(),
        unfocusedLabelColor=colorSecondary(),
        disabledLabelColor=colorSecondary(),
        errorLabelColor=colorSecondary(),
        placeholderColor=colorSecondary(),
        disabledPlaceholderColor=colorSecondary(),
        focusedBorderColor=Color.Unspecified,
        unfocusedBorderColor=Color.Unspecified
    )

    var yearFocused by remember{mutableStateOf(false)}
    var monthFocused by remember{mutableStateOf(false)}
    var dayFocused by remember{mutableStateOf(false)}

    Row{
        OutlinedTextField(
            value=year.value,
            onValueChange={newValue->
                if(newValue.length<=4&&Utils.isStringANumber(newValue)){
                    year.value=newValue
                    if(Utils.canBeIntParsed(newValue)){
                        if(Utils.isValidPastOrPresentYear(newValue.toInt())){
                            monthFocusRequester.requestFocus()
                            yearFocused=false
                            onYearValueChange(newValue)
                        }
                    }
                }
            },
            placeholder={
                BetterText("YYYY",fontSize="XS")
            },
            keyboardOptions=KeyboardOptions.Default.copy(keyboardType=KeyboardType.Number,imeAction=ImeAction.Next),
            modifier=Modifier
                .width(100.dp)
                .padding(end=3.dp)
                .border(
                    width=if(yearFocused) 2.dp else 1.dp,
                    color=if(yearFocused) colorSecondary() else colorTertiary()
                )
                .onFocusChanged {focusState-> yearFocused=focusState.isFocused}
                .focusRequester(yearFocusRequester),

            singleLine=true,
            colors=colors,
            textStyle=if(yearFocused)TextStyle.Default.copy(fontSize=18.sp) else TextStyle.Default.copy(fontSize=16.sp)
        )


        OutlinedTextField(
            value=month.value,
            onValueChange={newValue->
                if(newValue.length<=2&&Utils.isStringANumber(newValue)){
                    month.value=newValue
                    if(Utils.canBeIntParsed(newValue)){
                        if((1..12).contains(newValue.toInt())){
                            onMonthValueChange(newValue)
                            if((2..12).contains(newValue.toInt())){
                                dayFocusRequester.requestFocus()
                            }
                        }
                    }
                }

            },
            placeholder={
                BetterText("MM", textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth(),fontSize="XS")
            },
            keyboardOptions=KeyboardOptions.Default.copy(keyboardType=KeyboardType.Number,imeAction=ImeAction.Next),
            modifier=Modifier
                .width(70.dp)
                .border(
                    width=if(monthFocused) 2.dp else 1.dp,
                    color=if(monthFocused) colorSecondary() else colorTertiary()
                )
                .onFocusChanged {focusState-> monthFocused=focusState.isFocused}
                .focusRequester(monthFocusRequester),
            singleLine=true,
            colors=colors,
            textStyle=if(monthFocused)TextStyle.Default.copy(fontSize=18.sp) else TextStyle.Default.copy(fontSize=16.sp)
        )


        OutlinedTextField(
            value=day.value,
            onValueChange={newValue->
                if(newValue.length<=2&&Utils.isStringANumber(newValue)){
                    day.value=newValue
                    if(Utils.canBeIntParsed(newValue)){
                        if((1..31).contains(newValue.toInt())){
                            if((4..31).contains(newValue.toInt())){
                                focusManager.clearFocus()
                            }
                            onDayValueChange(newValue)
                        }
                    }
                }
            },
            placeholder={
                BetterText("DD",textAlign=TextAlign.Center,modifier=Modifier.fillMaxWidth(),fontSize="XS")
            },
            keyboardOptions=KeyboardOptions.Default.copy(keyboardType=KeyboardType.Number,imeAction=ImeAction.Done),
            modifier=Modifier
                .width(70.dp)
                .padding(start=3.dp)
                .border(
                    width=if(dayFocused) 2.dp else 1.dp,
                    color=if(dayFocused) colorSecondary() else colorTertiary()
                )
                .onFocusChanged {focusState-> dayFocused=focusState.isFocused}
                .focusRequester(dayFocusRequester),
            singleLine=true,
            colors=colors,
            textStyle=if(dayFocused)TextStyle.Default.copy(fontSize=18.sp) else TextStyle.Default.copy(fontSize=16.sp)
        )
    }
}

@Composable
fun colorPrimary():Color{
    return MaterialTheme.colors.background
}
@Composable
fun colorSecondary():Color{
    return MaterialTheme.colors.onBackground
}
@Composable
fun colorTertiary():Color{
    return MaterialTheme.colors.primary
}


@Composable
fun CustomizableDialog(
    onDismissRequest:()->Unit,
    content:@Composable ()->Unit
){
    Dialog(onDismissRequest={onDismissRequest()},properties=DialogProperties(usePlatformDefaultWidth=false)){
        Card(
            modifier=Modifier.heightIn(max=550.dp,min=250.dp).fillMaxWidth().padding(horizontal=16.dp),
            shape=RoundedCornerShape(15.dp),
        ){
            Box(modifier=Modifier.fillMaxSize().background(color=colorPrimary())){content()}
        }
    }
}




/*
@Composable
fun StyledTimePicker(
    onTimeSelected:(Int,Int)->Unit,
    time:TimePickerTime
){
    TimePicker(
        time=time,
        modifier=Modifier
            .fillMaxWidth()
            .padding(horizontal=20.dp),
        configuration=TimePickerConfiguration.Builder()
            .height(height=300.dp)
            .timeTextStyle(DefaultDatePickerConfig.dateTextStyle.copy(color=colorQuaternary(), fontSize=18.sp))
            .selectedTimeTextStyle(textStyle=TextStyle(color=colorSecondary(), fontSize=20.sp))
            .selectedTimeAreaColor(color=colorTertiary())
            .selectedTimeAreaShape(shape=RectangleShape)
            .selectedTimeAreaHeight(36.dp)
            .build(),
        onTimeSelected=onTimeSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDatePicker(
    state:DatePickerState
){
    DatePicker(
        title=null,
        headline=null,
        state=state,
        showModeToggle=false,
        colors=TimeUtils.datePickerColors(),
        modifier=Modifier.padding(all=0.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    state:DatePickerState,
    onClose:()->Unit,
    confirmButton:@Composable ()->Unit,
    onDismissRequest:()->Unit={}
){
    DatePickerDialog(
        onDismissRequest={onDismissRequest()},
        confirmButton={
            Row(modifier=Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){
                BetterButton(
                    onClick={onClose()},
                    modifier=Modifier
                        .padding(bottom=10.dp,start=30.dp,end=30.dp)
                        .fillMaxWidth()
                        .height(40.dp)
                ){
                    BetterText(text="Close",fontSize=18.sp)
                }
            }
            confirmButton()
        },
        colors=TimeUtils.datePickerColors()
    ){
        DatePicker(
            title=null,
            headline=null,
            state=state,
            showModeToggle=false,
            colors=TimeUtils.datePickerColors(),
            modifier=Modifier.padding(all=0.dp)
        )
    }
}

*/
