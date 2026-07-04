import sys
import re

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

# Replace variables and validation in LoginScreen
content = content.replace('var phoneNumber by remember { mutableStateOf("") }', 'var phoneNumber by remember { mutableStateOf("") } // Used as username')
content = content.replace('val isPhoneValid = phoneNumber.isNotEmpty() && phoneNumber.length >= 10 && phoneNumber.all { it.isDigit() }', 'val isPhoneValid = phoneNumber.isNotEmpty()')
content = content.replace('onValueChange = { if (it.all { char -> char.isDigit() }) phoneNumber = it },', 'onValueChange = { phoneNumber = it },')
content = content.replace('Text("يرجى إدخال رقم هاتف صحيح"', 'Text("يرجى إدخال اسم مستخدم أو رقم هاتف صحيح"')
content = content.replace('label = { Text("رقم الهاتف") }', 'label = { Text("اسم المستخدم / رقم الهاتف") }')
content = content.replace('prefix = { Text("+964 ", color = textSecondaryColor) },', '') # Remove the +964 prefix since it could be a username

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)
print("Patched LoginScreen")
