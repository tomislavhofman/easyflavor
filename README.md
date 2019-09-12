# EasyFlavor

**EasyFlavor** is a lightweight dependency injection lib for apps with several configurations/flavors that use a common codebase which slightly differs based on the flavor type.

Take, for example, an app that has a trial and a full version. Their codebase is exactly the same, but the trial version has some features taken out, and some parts of the code behaving differently than the full version. With **EasyFlavor**, the differences can neatly be put in a clean architecture using a few annotations, and the boilerplate code generated automatically. Naturally, the library can scale as much as possible, handling large projects with many different flavors.

The architecture enforced by the library fits neatly into popular options, such as MVP or MVVM.

**EasyFlavor** uses no Android dependencies so it's shipped as a Java library, meaning that it can be used in pure Java and Android projects alike.

The library works with **Kotlin** as well as Java, as illustrated by the [demo app](app/).

### Installation

EasyFlavor is hosted on JitPack - just add the JitPack maven repo to your list of repositories, and then add the EasyFlavor dependency and annotation processor:

```gradle
allprojects {
  repositories {
      jcenter()
      maven { url "https://jitpack.io" }
   }
}
```

```gradle
dependencies {
   implementation 'com.github.globulus:easyflavor:-SNAPSHOT'
   annotationProcessor 'com.github.globulus:easyflavor-processor:-SNAPSHOT'
   // and/or
   kapt 'com.github.globulus:easyflavor-processor:-SNAPSHOT'
}
```

### How to use

1. Create an interface for the part of code that depends on flavors, and *annotate it with* **@Flavorable**. E.g, here's an interface describing a View Model of a MainActivity:

```java
@Flavorable
interface MainActivityViewModel {
    void fetchData(String collection, Callback callback);
}
```

2. Provide a common-code implementation of the interface. **Its name must be the same as that of the interface + Impl.** Annotate methods that depend on flavors with **@FlavorInject**. Use **mode** to tell if the flavor injected code is supposed to be executed *before* or *after* the common code:

```java
class MainActivityViewModelImpl implements MainActivityViewModel {
    @FlavorInject(mode = FlavorInject.Mode.BEFORE)
    void fetchData(String collection, Callback callback) {
         Log.e(this.getClass().getSimpleName(), "Common code called");
    }
}
```

3. Define your app flavors and tell EasyFlavor how to resolve which flavor the app is running:

```java
class MyApp extends Application {
    public static final String FULL = "full";
    public static final String FREE = "free";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // FlavorResolver is a simple functional interface that returns
        // a String describing the app flavor. It's up to you to define
        // exactly how is the app flavor determined.
        EasyConfig.setResolver(() -> (BuildConfig.FULL_VERSION) ? FULL : FREE);
    }
}
```

4. Provide flavor-specific implementations of the interface. Annotate them with **@Flavored**, providing an array of flavors as Strings for which this implementation is valid:

```java
@Flavored(flavors = {MyApp.FREE})
class FreeMainActivityViewModel implements MainActivityViewModel {
    void fetchData(String collection, Callback callback) {
        String tag = this.getClass().getSimpleName();
        Log.e(tag, "FREE called for " + collection);
        callback.handle(tag);
    }
}
```

```java
@Flavored(flavors = {MyApp.FULL})
class FullMainActivityViewModel implements MainActivityViewModel {
    void fetchData(String collection, Callback callback) {
        String tag = this.getClass().getSimpleName();
        Log.e(tag, "FULL called for " + collection);
        callback.handle(tag);
    }
}
```

5. When instantiating the View Model, use **EasyFlavor.get(CLASS)** instead of instantiating manually:
```java
class MainActivity extends Activity {
    
    private MainActivityViewModel viewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        viewModel = EasyFlavor.get(MainActivityViewModel.class);
    }
}
```

6. PROFIT! Whenever any annotated method of the ViewModel is invoked, EasyFlavor will inject the flavor-specific ViewModel code before or after the common one.
