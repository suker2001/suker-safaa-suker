import sys

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'r') as f:
    content = f.read()

bad_def = """    // Custom Animated Toast State
    var toastMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            toastMessage = message
            delay(3000)
            toastMessage = null
        }
    }"""

good_def = """    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }"""

content = content.replace(bad_def, good_def)

with open('app/src/main/java/com/example/ui/screens/MainAppScreen.kt', 'w') as f:
    f.write(content)

