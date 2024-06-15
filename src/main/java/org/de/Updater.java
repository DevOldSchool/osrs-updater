package org.de;

import org.de.analysers.*;
import org.de.deobfuscation.*;
import org.de.utilities.JarUtil;
import org.de.utilities.MultiplierAnalyser;
import org.de.utilities.Multipliers;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Updater {
    public static int gamepackRevision = -1;
    public static List<ClassNode> classes = new ArrayList<>();
    public static List<Deobfuscator> deobfuscators = new ArrayList<>();
    public static List<Analyser> analysers = new ArrayList<>();

    public static void execute(String pathToJar, boolean gamepackToFile, boolean resultsToFile) throws IOException {
        long begin = System.currentTimeMillis();
        File jarFile = new File(pathToJar);

        if (!jarFile.exists()) {
            System.out.printf("JAR File does not exist. Path=%s\n", pathToJar);
            System.exit(1);
        }

        classes = JarUtil.loadNodes(jarFile);
        long end = (System.currentTimeMillis() - begin);

        // Create a FileOutputStream to write to the file
        FileOutputStream fos = new FileOutputStream(String.format("hooks/%s.txt", gamepackRevision));
        PrintStream printStream = new PrintStream(fos);
        if (resultsToFile) {
            System.setOut(printStream);
        }

        System.out.printf("Gamepack Revision: %s\n", Updater.gamepackRevision);
        System.out.printf("Finished loading %d classes in %d ms\n", classes.size(), end);

        // Extract multipliers
        MultiplierAnalyser.findMultipliers(classes);
        Multipliers.decideMultipliers();
        System.out.printf("Stored %d multipliers\n", Multipliers.multipliers.size());
        System.out.print("\n");

        // Execute deobfuscation
        addDeobfuscators();
        for (Deobfuscator deobfuscator : deobfuscators) {
            deobfuscator.initialize();
            deobfuscator.execute(classes);
            deobfuscator.onCompletion();
            JarUtil.recomputeMaxsForClasses(classes);
        }

        // Execute analysers
        addAnalysers();
        int foundFields = 0;
        int totalFields = 0;
        int foundMethods = 0;
        int totalMethods = 0;
        int foundClasses = 0;

        begin = System.currentTimeMillis();
        for (Analyser analyser : analysers) {
            analyser.execute(classes);

            if (analyser.isBroken()) {
                System.out.printf("\n\t-> %s is broken.\n", analyser.getClass().getSimpleName());
            } else {
                foundFields += analyser.getFieldCount();
                totalFields += analyser.getExpectedFieldsSize();
                foundMethods += analyser.getMethodCount();
                totalMethods += analyser.getExpectedMethodsSize();
                foundClasses++;
                analyser.print();
            }
        }
        end = (System.currentTimeMillis() - begin);

        System.out.print("\n");
        System.out.printf("Identified %d/%d classes\n", foundClasses, analysers.size());
        System.out.printf("Identified %d/%d methods\n", foundMethods, totalMethods);
        System.out.printf("Identified %d/%d fields\n", foundFields, totalFields);
        System.out.printf("Finished analysing in %s ms\n", end);

        // Rename based on analysers
        if (gamepackToFile) {
            Renamer renamer = new Renamer();
            renamer.initialize();
            renamer.execute(classes, analysers);
            renamer.onCompletion();
            JarUtil.recomputeMaxsForClasses(classes);

            // Save deobfuscated classes for manual analysis
            JarUtil.saveClassesToDisk(classes, String.format("gamepacks/%s/", gamepackRevision));
        }

        // Close the PrintStream and FileOutputStream
        if (resultsToFile) {
            printStream.close();
            fos.close();
        }
    }

    public static void reset() {
        // Add to main analysers for cross revision checks
        List<Analyser> copyOfAnalysers = new ArrayList<>(analysers);
        Main.analysers.put(gamepackRevision, copyOfAnalysers);

        // Reset fields
        classes.clear();
        analysers.clear();
        deobfuscators.clear();
        gamepackRevision = -1;
        Multipliers.reset();
    }

    private static void addDeobfuscators() {
        deobfuscators.add(new RuntimeExceptions());
        deobfuscators.add(new DeadCode());
        deobfuscators.add(new ControlFlowOptimiser());
        deobfuscators.add(new ExceptionRangeOptimiser());
        deobfuscators.add(new IllegalStateExceptions());
        deobfuscators.add(new UnusedArguments());
        deobfuscators.add(new JumpOptimiser());
//        deobfuscators.add(new ControlFlow());
        deobfuscators.add(new ConstructorErrors());
        deobfuscators.add(new GetPaths());

        deobfuscators.add(new FieldMover());
        deobfuscators.add(new UnusedFields());
        deobfuscators.add(new FieldSorter());

        deobfuscators.add(new MethodMover());
        deobfuscators.add(new UnusedMethods());
        deobfuscators.add(new MethodSorter());

        deobfuscators.add(new InlineStrings());
        deobfuscators.add(new MultiplierRemover());
        deobfuscators.add(new DecomplierTrap());
    }

    private static void addAnalysers() {
        analysers.add(new Canvas());
        analysers.add(new Node());
        analysers.add(new Link());
        analysers.add(new LinkedList());
        analysers.add(new RSException());
        analysers.add(new Inflater());
        analysers.add(new ItemStorage());
        analysers.add(new ByteBuffer());
        analysers.add(new ByteArrayNode());
        analysers.add(new AbstractByteBuffer());
        analysers.add(new BasicByteBuffer());
        analysers.add(new Producer());
        analysers.add(new ComponentProducer());
        analysers.add(new HashTable());
        analysers.add(new FixedSizeDeque());
        analysers.add(new FixedSizeDequeIterator());
        analysers.add(new NodeList());
        analysers.add(new NodeListIterator());
        analysers.add(new DualNode());
        analysers.add(new Wrapper());
        analysers.add(new SoftReference());
        analysers.add(new HardReference());
        analysers.add(new DoublyNode());
        analysers.add(new DoublyNodeIterator());
        analysers.add(new ClientPreferences());
        analysers.add(new RenderableNode());
        analysers.add(new Queue());
        analysers.add(new Cache());
        analysers.add(new InventoryDefinition());
        analysers.add(new ISAACCipher());
        analysers.add(new PacketBuffer());
        analysers.add(new OutgoingPacketMeta());
        analysers.add(new OutgoingPacket());
        analysers.add(new RegionUpdatePacketHeader());
        analysers.add(new IncomingPacketMeta());
        analysers.add(new AsynchronousInputStream());
        analysers.add(new AsynchronousOutputStream());
        analysers.add(new Socket());
        analysers.add(new AbstractSocket());
        analysers.add(new PacketContext());
        analysers.add(new IdentityTable());
        analysers.add(new AbstractArchive());
        analysers.add(new Rasterizer2D());
        analysers.add(new AbstractRasterizer());
        analysers.add(new Sprite());
        analysers.add(new AbstractFont());
        analysers.add(new Skeleton());
        analysers.add(new Animation());
        analysers.add(new Frames());
        analysers.add(new Model());
        analysers.add(new AppearanceCustomization());
        analysers.add(new AnimationSequence());
        analysers.add(new ModelHeader());
        analysers.add(new ItemDefinition());
        analysers.add(new DefinitionProperty());
        analysers.add(new ObjectDefinition());
        analysers.add(new NpcDefinition());
        analysers.add(new FloorUnderlayDefinition());
        analysers.add(new SpotAnim());
        analysers.add(new PlayerDefinition());
        analysers.add(new IndexedImage());
        analysers.add(new RuneScript());
        analysers.add(new AccessFile());
        analysers.add(new AccessFileHandler());
        analysers.add(new ArchiveDisk());
        analysers.add(new Archive());
        analysers.add(new FileSystemRequest());
        analysers.add(new ClassInfo());
        analysers.add(new NameComposite());
        analysers.add(new Nameable());
        analysers.add(new PlayerType());
        analysers.add(new AbstractRawAudioNode());
        analysers.add(new Resampler());
        analysers.add(new RawAudioNode());
        analysers.add(new TileItem());
        analysers.add(new DynamicObject());
        analysers.add(new Projectile());
        analysers.add(new Varp());
        analysers.add(new VarPlayerType());
        analysers.add(new Task());
        analysers.add(new Signlink());
        analysers.add(new Deque());
        analysers.add(new MouseTracker());
        analysers.add(new CollisionMap());
        analysers.add(new AudioEnvelope());
        analysers.add(new SoundFilter());
        analysers.add(new AudioInstrument());
        analysers.add(new AudioEffect());
        analysers.add(new MovementType());
        analysers.add(new Actor());
        analysers.add(new Npc());
        analysers.add(new ChatSetting());
        analysers.add(new Player());
        analysers.add(new MenuRowContext());
        analysers.add(new AttackOptionSetting());
        analysers.add(new GrandExchangeOffer());
        analysers.add(new MouseListener());
        analysers.add(new MouseWheelListener());
        analysers.add(new KeyInputData());
        analysers.add(new KeyboardListener());
        analysers.add(new KeyInputHandler());
        analysers.add(new TileModel());
        analysers.add(new TilePaint());
        analysers.add(new JagexLoginType());
        analysers.add(new ItemLayer());
        analysers.add(new BoundaryObject());
        analysers.add(new InteractableObject());
        analysers.add(new AnimableObject());
        analysers.add(new WallDecoration());
        analysers.add(new FloorDecoration());
        analysers.add(new Tile());
        analysers.add(new Region());
        analysers.add(new PendingSpawn());
        analysers.add(new WidgetNode());
        analysers.add(new ChatboxMessage());
        analysers.add(new ChatboxChannel());
        analysers.add(new BufferedImage());
        analysers.add(new Widget());
        analysers.add(new ScriptEvent());
        analysers.add(new ScriptState());
        analysers.add(new RuneScriptVM());
        analysers.add(new Server());
        analysers.add(new RSShadowedFont());
        analysers.add(new CombatBarData());
        analysers.add(new CombatBar());
        analysers.add(new CombatBarDefinition());
        analysers.add(new TaskDataNode());
        analysers.add(new AbstractSoundSystem());
        analysers.add(new SoundSystem());
        analysers.add(new AudioTask());
        analysers.add(new AudioRequestNode());
        analysers.add(new AreaSoundEmitter());
        analysers.add(new AudioRunnable());
        analysers.add(new AudioTrack());
        analysers.add(new AbstractNameableComparator());
        analysers.add(new NameableContainer());
        analysers.add(new FriendContainer());
        analysers.add(new FriendListLink());
        analysers.add(new IgnoreContainer());
        analysers.add(new ChatPlayer());
//        analysers.add(new ChatPlayerComparator());
        analysers.add(new IgnoredMessage());
        analysers.add(new FriendMessage());
        analysers.add(new ClanMember());
        analysers.add(new FriendManager());
        analysers.add(new ClanContainer());
        analysers.add(new Occluder());
        analysers.add(new Hitsplat());
        analysers.add(new IntegerNode());
        analysers.add(new ObjectNode());
        analysers.add(new UrlRequest());
        analysers.add(new AbstractRequester());
        analysers.add(new UrlRequester());
        analysers.add(new AppletParameter());
        analysers.add(new PlatformInfo());
        analysers.add(new PlatformInfoProvider());
        analysers.add(new WorldMapLabelSize());
        analysers.add(new WorldMapLabel());
        analysers.add(new Coordinate());
        analysers.add(new AbstractWorldMapIcon());
        analysers.add(new WorldMapDecoration());
        analysers.add(new AbstractWorldMapData());
        analysers.add(new WorldMapArea());
        analysers.add(new WorldMapRenderer());
        analysers.add(new WorldMapRectangle());
        analysers.add(new WorldMapArchiveLoader());
        analysers.add(new WorldMapSprite());
        analysers.add(new WorldMap());
        analysers.add(new KitDefinition());
        analysers.add(new FileRequest());
        analysers.add(new Overlay());
        analysers.add(new HorizontalAlignment());
        analysers.add(new VerticalAlignment());
        analysers.add(new WorldMapElement());
        analysers.add(new Enumerated());
        analysers.add(new WorldMapSectionType());
        analysers.add(new Bounds());
        analysers.add(new NanoTimer());
        analysers.add(new AbstractTimer());
        analysers.add(new MilliTimer());
        analysers.add(new JagexGame());
        analysers.add(new LanguageType());
        analysers.add(new BuildType());
        analysers.add(new GameShell());
        analysers.add(new RouteStrategy());
        analysers.add(new ApproximateRouteStrategy());
        analysers.add(new FileSystemRequestHandler());
        analysers.add(new ArchiveLoader());
        analysers.add(new ActorSpotAnim());

        if (gamepackRevision >= 215) {
            analysers.add(new AddRequestTask());
        }

        if (gamepackRevision >= 222) {
            analysers.add(new WorldView());
        }

        analysers.add(new Client());
    }
}
