package burp.hv;

import burp.*;
import burp.api.montoya.BurpExtension;
import burp.api.montoya.EnhancedCapability;
import burp.api.montoya.MontoyaApi;
import burp.hv.settings.Settings;
import burp.hv.tags.CustomTags;
import burp.hv.tags.Tag;
import burp.hv.ui.ContextMenu;
import burp.hv.ui.ExtensionPanel;
import burp.hv.ui.HackvertorMessageTab;
import burp.hv.utils.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.security.Security;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static burp.hv.Convertors.*;

public class HackvertorExtension implements BurpExtension, IBurpExtender, ITab, IExtensionStateListener, IMessageEditorTabFactory {
    //TODO Unset on unload
    public static String extensionName = "Hackvertor";
    public static String version = "v2.0.12";
    public static JFrame HackvertorFrame = null;
    public static IBurpExtenderCallbacks callbacks;
    public static IExtensionHelpers helpers;
    public static String tagCodeExecutionKey = null;
    public static Ngrams ngrams;
    public static PrintWriter stderr;
    public static PrintWriter stdout;
    public static MontoyaApi montoyaApi;
    public static Settings generalSettings;
    public static HashMap<String,String>globalVariables = new HashMap<>();
    public static boolean isNativeTheme;
    public static boolean isDarkTheme;
    private List<String> NATIVE_LOOK_AND_FEELS = Arrays.asList("GTK","Windows","Aqua","FlatLaf - Burp Light");
    public static List<String> DARK_THEMES = Arrays.asList("Darcula","FlatLaf - Burp Dark");

    public static Hackvertor hackvertor;
    public static ExtensionPanel extensionPanel;

    public static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static int requestHistoryPos = 0;
    public static ArrayList<IRequestInfo> requestHistory = new ArrayList<>();
    public static HashMap<String, Integer> tagCount = new HashMap<>();
    public static final HashMap<String, HashMap<String, Integer>> contextTagCount = new HashMap() {
        {
            put("GET", new HashMap<>());
            put("POST", new HashMap<>());
            put("JSON", new HashMap<>());
        }
    };
    private JMenuBar burpMenuBar;

    public static int MAX_POPULAR_TAGS = 10;

    @Override
    public Set<EnhancedCapability> enhancedCapabilities() {
        return Set.of(EnhancedCapability.AI_FEATURES);
    }

    @Override
    public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
        return new HackvertorMessageTab(hackvertor);
    }

    public static ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = HackvertorExtension.class.getResource(path);
        if (imgURL != null) {
            ImageIcon img = new ImageIcon(imgURL, description);
            Image resizedImage = img.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImage);
        } else {
            stderr.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static void print(String s){
        System.out.print(s);
        callbacks.printOutput(s);
    }

    public void registerExtenderCallbacks(final IBurpExtenderCallbacks burpCallbacks) {
        generalSettings = new Settings("general", burpCallbacks);
        Utils.registerGeneralSettings(generalSettings);
        generalSettings.load();
        callbacks = burpCallbacks;
        helpers = callbacks.getHelpers();
        stderr = new PrintWriter(callbacks.getStderr(), true);
        stdout = new PrintWriter(callbacks.getStdout(), true);
        tagCodeExecutionKey = CustomTags.generateRandomCodeExecutionKey();
        callbacks.setExtensionName(extensionName);
        Security.addProvider(new BouncyCastleProvider());
        SwingUtilities.invokeLater(() -> {
            try {
                hackvertor = new Hackvertor();
                stdout.println(extensionName + " " + version);
                CustomTags.loadCustomTags();
                Variables.loadGlobalVariables();
                registerPayloadProcessors();
                extensionPanel = new ExtensionPanel(hackvertor);
                callbacks.addSuiteTab(this);
                if(montoyaApi == null) {
                    ExecutorService service = Executors.newSingleThreadExecutor();
                    try (Closeable close = service::shutdown) {
                        service.submit(() -> {
                            burpMenuBar = null;
                            while(burpMenuBar == null) {
                                burpMenuBar = Objects.requireNonNull(getBurpFrame()).getJMenuBar();
                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    break;
                                }
                            }
                            burpMenuBar.add(Utils.generateHackvertorMenuBar());
                        });
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        service.shutdown();
                    }
                }
                callbacks.registerMessageEditorTabFactory(HackvertorExtension.this);
                callbacks.registerContextMenuFactory(new ContextMenu());
                callbacks.registerHttpListener(new HttpListener());
                callbacks.registerExtensionStateListener(this);
            } catch (Exception ignored){

            }
        });
        //callbacks.printOutput("Look And Feel: "+UIManager.getLookAndFeel().getID());
        isNativeTheme = NATIVE_LOOK_AND_FEELS.contains(UIManager.getLookAndFeel().getID());
        isDarkTheme = DARK_THEMES.contains(UIManager.getLookAndFeel().getID());
    }

    void registerPayloadProcessors() {
        ArrayList<Tag> tags = hackvertor.getTags();
        for(int i=0;i<tags.size();i++) {
            Tag tag = tags.get(i);
            if(tag.argument1 == null) {
                if(tag.name.startsWith("_")) {
                    continue;
                }
                callbacks.registerIntruderPayloadProcessor(new HackvertorPayloadProcessor(hackvertor, extensionName+ "_" + capitalise(tag.name), tag.name));
            }
        }
    }

    public void extensionUnloaded() {
        if(HackvertorFrame != null) {
            HackvertorFrame.removeAll();
            HackvertorFrame.dispose();
            HackvertorFrame = null;
        }
        executorService.shutdownNow();
        ngrams = null;
        if(montoyaApi == null) {
            SwingUtilities.invokeLater(() -> {
                for (int i = 0; i < burpMenuBar.getMenuCount(); i++) {
                    JMenu menu = burpMenuBar.getMenu(i);
                    if (menu != null && extensionName.equals(menu.getText())) {
                        burpMenuBar.remove(menu);
                        break;
                    }
                }
                burpMenuBar.revalidate();
                burpMenuBar.repaint();
                burpMenuBar = null;
            });
        }
        callbacks.printOutput(extensionName + " unloaded");
    }

    private static JFrame getBurpFrame() {
        for (Frame f : Frame.getFrames()) {
            if (f.isVisible() && f.getTitle().startsWith(("Burp Suite"))) {
                return (JFrame) f;
            }
        }
        return null;
    }

    public String getTabCaption() {
        return extensionName;
    }

    public static void alert(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }

    public Component getUiComponent() {
        return extensionPanel;
    }

    //Used in tests
    public static void setHelpers(IExtensionHelpers helpers) {
        HackvertorExtension.helpers = helpers;
    }

    @Override
    public void initialize(MontoyaApi montoyaApi) {
        HackvertorExtension.montoyaApi = montoyaApi;
        montoyaApi.userInterface().menuBar().registerMenu(Utils.generateHackvertorMenuBar());
    }
}
