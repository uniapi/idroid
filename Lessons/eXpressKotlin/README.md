# 🚄eXpress Kotlin

## `if`
In Kotlin, the `if` construction is a _control flow statement_, but just like `when` (see below), it can also be used as an _expression_.
This means it can return a value directly, completely replacing the need for a traditional _ternary operator_ (condition ? true : false) found in Java or C++.
Here is a comprehensive guide to using `if` in Kotlin:
### 1. Basic Usage (Traditional Statement)
You can use `if` just like in any other language to execute a block of code conditionally.
```kotlin
val currentLayoutWidth = 320
if (currentLayoutWidth < 400) {
    Log.d(TAG, "Layout is too narrow for standard paddings")
} else {
    Log.d(TAG, "Layout width is fine")
}
```
### 2. Using if as an Expression (Returning a Value)
When you use `if` to assign a value to a variable, the `else` branch is **mandatory**. The last expression inside the chosen block becomes the returned value.
```kotlin
val marginInPx = 16val paddingInPx = 8
// The result of the if-else expression is assigned to maxSpacing
val maxSpacing = if (marginInPx > paddingInPx) {
    Log.d(TAG, "Margin is larger")
    marginInPx // This value is returned
} else {
    paddingInPx // This value is returned
}
```
For short assignments, you can write it neatly in a single line:
```kotlin
val backgroundStyle = if (isDarkTheme) Color.BLACK else Color.WHITE
```
### 3. Multiple Branches (if-else-if)
You can chain multiple conditions together. If used as an _expression_, an `else` branch **must close** the chain to cover all other possibilities.
```kotlin
val pressedRow = 2
val rowLabel = if (pressedRow == 1) {
    "Top row"
} else if (pressedRow == 2) {
    "Middle row"
} else {
    "Bottom row"
}
```
(Note: If you have more than two branches, Kotlin linting will usually suggest using a `when` construction instead, as it looks cleaner).
### 4. Combining with Kotlin's Safe Navigation (?: Elvis Operator)
A very common pattern in Kotlin is using `if` to check for null values or combining your condition checks with the _Elvis_ operator (**?:**).
```kotlin
val buttonText: String? = getSelectedButtonText()
// Traditional null check
if (buttonText != null) {
    println(buttonText.length)
}
// Cleaner alternative using Elvis operator instead of a full if-else block
val finalLength = buttonText?.length ?: 0
```

------------------------------
## `when`
In Kotlin, the `when` expression replaces the traditional `switch` statement found in other languages like Java or C++.
It is much more powerful, readable, and can be used as either a _statement_ (execute code) or an _expression_ (return a value).
Here are the most common ways to use the `when` construction:
### 1. Basic Usage (Like a Switch)
You match a variable against multiple branches.
```kotlin
val number = 2
when (number) {
    1 -> println("One")
    2 -> println("Two")
    3 -> println("Three")
    else -> println("Unknown number") // Acts like 'default' in Java
}
```
### 2. Combining Multiple Conditions
You can combine multiple values in a single branch using a comma.
```kotlin
val position = "ARM"
when (position) {
    "x86", "ARM" -> println("Supported Architecture")
    "SPARC", "RISC-V" -> println("Experimental Architecture")
    else -> println("Unsupported")
}
```
### 3. Using when as an Expression (Returning a Value)
If you use `when` to assign a value to a variable, the `else` branch is mandatory (unless the compiler is sure you covered all possible cases, like with Enums or Sealed Classes).
```kotlin
val validArch = "RISC-V"
val isModern = when (validArch) {
    "RISC-V", "ARM" -> true
    "x86", "SPARC" -> false
    else -> false
}

println("Is modern: $isModern")
```
### 4. Checking Ranges or Collections
You can check if a value is within a _range_ using the `in` keyword, or if it is inside a collection.
```kotlin
val clickedButton = 5
when (clickedButton) {
    in 1..3 -> println("First row clicked")
    in 4..6 -> println("Second row clicked")
    in 7..9 -> println("Third row clicked")
    0 -> println("Zero clicked")
    else -> println("Outside the keypad")
}
```
### 5. when Without an Argument (Replacing if-else-if)
If you don't pass an argument to `when`, it acts as a cleaner alternative to long `if-else` chains. Each branch condition must evaluate to a _boolean_ (true/false).
```kotlin
val selectedRow = 2val selectedCol = 3
when {
    selectedRow == 1 && selectedCol == 1 -> println("Top-left item")
    selectedRow == 3 && selectedCol == 2 -> println("Bottom-middle item (Zero)")
    else -> println("Any other item")
}
```
### 6. Checking Types (is keyword)
You can use `when` to check the data type of an object. Kotlin will automatically smart-cast it inside the branch.
```kotlin
val view: Any = "Hello"
when (view) {
    is Int -> println("It's an Integer, multiplied: ${view * 2}")
    is String -> println("It's a String, length: ${view.length}")
    else -> println("Unknown type")
}
```
### Statements (Executing Multiple Actions)
When you use `when` to execute code, the **curly braces** allow you to group multiple lines together for a single branch.
```kotlin
when (pressedButton) {
    "0" -> {
        // Expression 1: Log the event
        Log.d(TAG, "Zero button clicked")
        // Expression 2: Update the business logic
        currentInput += "0"
        // Expression 3: Update the UI text
        viewer.text = currentInput
    }
    else -> {
        Log.d(TAG, "Other button clicked: $pressedButton")
    }
}
```
### Expressions (Returning a Value from Block)
If you use `when` to assign a value to a variable, the very last line inside the **curly braces { ... }** will be treated as the return value for that branch.
```kotlin
val operationResult = when (actionCode) {
    1 -> {
        val initialCalculations = 10 * 5
        Log.d(TAG, "Processing branch 1")
        initialCalculations + 2 // This is the returned value
    }
    else -> {
        0 // Default returned value
    }
}
```

------------------------------
## Loops
In Kotlin, loops are used to execute a block of code repeatedly. There are _three main types of loops_: `for`, `while`, and `do-while`.
Here is a comprehensive guide to how they work in Kotlin:

### 1. The for Loop
The `for` loop in Kotlin iterates through anything that provides an iterator (like _ranges_, _arrays_, or _collections_).
It does not use the traditional index-based syntax found in Java or C++.
### Iterating over Ranges
You can loop through a sequence of numbers using the `..` operator, `until`, or `downTo`.
```kotlin
// 1 to 5 inclusive (1, 2, 3, 4, 5)
for (i in 1..5) {
    println(i)
}
// 1 to 5 exclusive (1, 2, 3, 4)
for (i in 1 until 5) {
    println(i)
}
// Counting downwards (5, 4, 3, 2, 1)
for (i in 5 downTo 1) {
    println(i)
}
// Using a custom step size (1, 3, 5)
for (i in 1..5 step 2) {
    println(i)
}
```
### Iterating over Arrays or Collections
You can iterate directly through values, indexes, or both.
```kotlin
val list = listOf("Apple", "Banana", "Cherry")
// Direct value iteration
for (item in list) {
    println(item)
}
// Index iteration using .indices
for (index in list.indices) {
    println("Index $index holds ${list[index]}")
}
// Both index and value using .withIndex()
for ((index, value) in list.withIndex()) {
    println("Item at $index is $value")
}
```
### 2. The while Loop
The `while` loop continuously executes its block of code as long as its conditional expression remains true.
It checks the condition before running the code.
```kotlin
var x = 3
while (x > 0) {
    println("Counting down: $x")
    x-- // Decrement to avoid infinite loop
}
```
### 3. The do-while Loop
The `do-while` loop executes the body of the loop at least once, and then evaluates the conditional expression at the bottom.
```kotlin
var y = 0
do {
    println("This prints at least once even if condition is false")
    y++
} while (y < 0)
```
### Loop Control: break and continue
Kotlin supports structural jump expressions inside loops:

* `break` terminates the nearest enclosing loop entirely.
* `continue` skips the current loop iteration and moves straight to the next one.
```kotlin
for (i in 1..5) {
    if (i == 2) continue // Skip number 2
    if (i == 4) break    // Stop the loop completely when reaching 4
    println(i)           // Prints: 1, 3
}
```
### Alternative: Functional Loops `forEach` and `forEachIndexed`
Kotlin collections also support _inline lambda expressions_ for a cleaner, modern look:
```kotlin
val numbers = arrayOf(10, 20, 30)

numbers.forEach { num ->
    println(num)
}
```
```kotlin
val systems = arrayOf("Linux", "Android", "Windows", "macOS")

systems.forEachIndexed { index, sys ->
    println("Index: $index, System: $sys")
}
```

------------------------------
## Scope Functions
In Kotlin, the main purpose of **Scope Functions** is to execute a block of code within the context of a specific object,
making your code cleaner and reducing the need to repeat variable names.
The differences between them come down to two simple things:

   1. How they refer to the object inside the block (`this` vs `it`).
   2. What they return (**the object itself** vs **the result of the last line**).
### Quick Reference Matrix
| Function | Context Object Referenced as... | Return Value | Main Use Case |
|---|---|---|---|
| `apply` | `this` (can be omitted) | The object itself | Initializing/configuring an object. |
| `also` | `it` (can be renamed) | The object itself | Side effects, logging, extra actions. |
| `let` | `it` (can be renamed) | Result of the last line | Null-safety checks (`?.let`), mapping. |
| `run` | `this` (can be omitted) | Result of the last line | Object configuration + computing a result. |
| `with` | `this` (passed as argument) | Result of the last line | Operating on an object without repeating its name. |

### 1. `apply` — "Configure this object and give it back to me"
Inside `apply`, the object is `this`. You can access its properties and methods directly without typing the object's name.
It always returns the object itself.

**Best for**: Setting up Android Views programmatically.
```kotlin
val row = TableRow(this).apply {
    layoutParams = rowParams // same as this.layoutParams
    gravity = Gravity.CENTER // same as this.gravity
} // Returns the fully configured TableRow instance
```
### 2. `also` — "Do this extra action with the object, then give it back"
Inside `also`, the object is `it`. It returns the object itself. It is great for tasks that don't alter the object, like logging or printing.

**Best for**: Logging or debugging chain steps.
```kotlin
val button = Button(this)
    .apply { text = "Apply" }
    .also { println("Log: Created button with text: ${it.text}") }
```
### 3. `let` — "Use this object to calculate a result, or check if it's not null"
Inside `let`, the object is `it`. It returns whatever the last line of the block evaluates to.

**Best for**: Null-safety checks (`?.let`) or transforming data.
```kotlin
// 1. Safe execution if not null
var row: TableRow? = fetchRow()
row?.let { finishedRow ->
    table.addView(finishedRow) // Executes only if row is not null
}
// 2. Transforming data
val finalIndex = keypad.lastIndex.let { index ->
    "The last index is $index"
} // finalIndex is a String: "The last index is 9"
```
### 4. `run` — "Configure this object and compute a result"
Inside `run`, the object is `this`. It returns the result of the last line. It works exactly like a combination of `apply` and `let`.

**Best for**: Complex calculation blocks where you need to compute something using the object's properties.
```kotlin
val isKeypadValid = table.run {
    isStretchAllColumns = true
    childCount > 0 // Last line evaluates to a Boolean
} // isKeypadValid is now true or false
```
### 5. `with` — "With this object, do the following"
Unlike the others, `with` is not an extension function. You pass the object as an argument. Inside the block, the object is `this`.
It returns the result of the last line.

**Best for**: Grouping multiple method calls on an object when you don't care about the return value.
```kotlin
with(root) {
    addView(viewer)
    addView(table)
    setBackgroundColor(Color.BLACK)
}
```
### Tips
* If you want to **initialize a view** and keep using it: Use `apply`.
* If you want to check if a **variable is null** before running code: Use `?.let`.
* If you want to group operations on a **parent container**: Use `with`.
