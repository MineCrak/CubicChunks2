package io.github.opencubicchunks.cubicchunks.debug;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCurrentContext;
import static org.lwjgl.glfw.GLFW.glfwGetMonitorPos;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glFinish;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindBuffer;
import static org.lwjgl.opengl.GL20.glBufferData;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGenBuffers;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import io.github.opencubicchunks.cubicchunks.chunk.ICubeHolder;
import io.github.opencubicchunks.cubicchunks.chunk.LightHeightmapGetter;
import io.github.opencubicchunks.cubicchunks.chunk.heightmap.SurfaceTrackerSection;
import io.github.opencubicchunks.cubicchunks.chunk.util.CubePos;
import io.github.opencubicchunks.cubicchunks.utils.Coords;
import io.github.opencubicchunks.cubicchunks.utils.MathUtil;
import it.unimi.dsi.fastutil.longs.Long2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.LevelStem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class DebugVisualization {
    private static final boolean HEIGHTMAP_VIEW_ENABLED = System.getProperty("cubicchunks.debug.heightmap", "false").equals("true");

    private static final String VERT_SHADER =
        "#version 330 core\n" +
            "layout(location = 0) in vec3 posIn;\n" +
            "layout(location = 1) in vec4 colorIn;\n" +
            "smooth out vec4 fragColor;\n" +
            "uniform mat4 mvpMatrix;\n" +
            "void main() {\n" +
            "  gl_Position = mvpMatrix * vec4(posIn, 1);\n" +
            "  fragColor = colorIn;\n" +
            "}";
    private static final String FRAG_SHADER =
        "#version 330 core\n" +
            "smooth in vec4 fragColor;\n" +
            "out vec4 outColor;\n" +
            "void main() {\n" +
            "  outColor = fragColor;\n" +
            "}";
    private static final Logger LOGGER = LogManager.getLogger();

    private static volatile Level clientWorld;
    private static volatile Map<ResourceKey<?>, Level> serverWorlds = new ConcurrentHashMap<>();
    private static AtomicBoolean initialized = new AtomicBoolean();
    private static boolean shutdown = false;
    private static long window;
    private static int shaderProgram;
    private static int matrixLocation;
    private static int vao;
    private static int posAttrib;
    private static int colAttrib;
    private static int glBuffer;
    private static BufferBuilder bufferBuilder;
    private static BufferBuilder perfGraphBuilder;

    private static Matrix4f mvpMatrix = new Matrix4f();
    private static Matrix4f inverseMatrix = new Matrix4f();
    private static PerfTimer[] perfTimer = new PerfTimer[128];
    private static int perfTimerIdx = 0;
    private static float screenWidth = 854.0f;
    private static float screenHeight = 480f;
    private static GLCapabilities debugGlCapabilities;
    private static boolean enabled;

    private static PerfTimer timer() {
        if (perfTimer[perfTimerIdx] == null) {
            perfTimer[perfTimerIdx] = new PerfTimer();
        }
        return perfTimer[perfTimerIdx];
    }


    public static void enable() {
        enabled = true;
    }

    public static void onRender() {
        if (!enabled) {
            return;
        }
        if (shutdown) {
            return;
        }

        long ctx = glfwGetCurrentContext();

        GLCapabilities capabilities = GL.getCapabilities();
        if (!initialized.getAndSet(true)) {
            initializeWindow();
        }
        GL.setCapabilities(debugGlCapabilities);
        try {
            glfwMakeContextCurrent(window);
            render();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException interruptedException) {
                return;
            }
            try {
                bufferBuilder.end();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } finally {
            glfwMakeContextCurrent(ctx);
            GL.setCapabilities(capabilities);
        }
    }

    public static void onWorldLoad(Level w) {
        if (!enabled) {
            return;
        }
        if (w instanceof ClientLevel) {
            clientWorld = w;
        } else if (w instanceof ServerLevel) {
            serverWorlds.put(w.dimension(), w);
        }

    }

    public static void onWorldUnload(Level w) {
        if (!enabled) {
            return;
        }
        if (w instanceof ServerLevel) {
            serverWorlds.remove(w.dimension());
        }
    }

    public static void initializeWindow() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }


        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(854, 480, "CubicChunks debug", 0L, 0L);
        if (window == 0L) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer monPosLeft = stack.mallocInt(1);
            IntBuffer monPosTop = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwGetMonitorPos(glfwGetPrimaryMonitor(), monPosLeft, monPosTop);
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2 + monPosLeft.get(0),
                (vidmode.height() - pHeight.get(0)) / 2 + monPosTop.get(0));
        }

        initWindow();
    }

    private static void initWindow() {
        glfwShowWindow(window);
        glfwMakeContextCurrent(window);

        debugGlCapabilities = GL.createCapabilities();
        initialize();
        glfwSwapBuffers(window);
    }

    private static void initialize() {
        int vert = glCreateShader(GL_VERTEX_SHADER);
        System.out.println("E0=" + glGetError());
        compileShader(vert, VERT_SHADER);
        System.out.println("E1=" + glGetError());
        int frag = glCreateShader(GL_FRAGMENT_SHADER);
        System.out.println("E2=" + glGetError());
        compileShader(frag, FRAG_SHADER);
        System.out.println("E3=" + glGetError());
        int program = glCreateProgram();
        System.out.println("E4=" + glGetError());
        linkShader(program, vert, frag);
        System.out.println("E5=" + glGetError());
        shaderProgram = program;
        posAttrib = glGetAttribLocation(shaderProgram, "posIn");
        colAttrib = glGetAttribLocation(shaderProgram, "colorIn");
        System.out.println("E6=" + glGetError());
        matrixLocation = glGetUniformLocation(shaderProgram, "mvpMatrix");
        System.out.println("E7=" + glGetError());
        vao = glGenVertexArrays();
        System.out.println("E8=" + glGetError());
        glBindVertexArray(vao);
        System.out.println("E9=" + glGetError());
        glBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, glBuffer);
        System.out.println("E10=" + glGetError());
        bufferBuilder = new BufferBuilder(4096);
        perfGraphBuilder = new BufferBuilder(4096);
    }

    private static void compileShader(int id, String shader) {
        glShaderSource(id, shader);
        glCompileShader(id);
        int status = glGetShaderi(id, GL_COMPILE_STATUS);
        int length = glGetShaderi(id, GL_INFO_LOG_LENGTH);
        if (length > 0) {
            String log = glGetShaderInfoLog(id);
            LOGGER.error(log);
            if (status != GL_TRUE) {
                throw new RuntimeException("Shader failed to compile, see log");
            }
        }
    }

    private static void linkShader(int program, int vert, int frag) {
        System.out.println("E5a=" + glGetError());
        glAttachShader(program, vert);
        System.out.println("E5b=" + glGetError());
        glAttachShader(program, frag);
        System.out.println("E5c=" + glGetError());
        glLinkProgram(program);
        System.out.println("p=" + program + ", v=" + vert + ", f=" + frag);
        System.out.println("E5d=" + glGetError());
        int status = glGetProgrami(program, GL_LINK_STATUS);
        System.out.println("E5e=" + glGetError() + " status=" + status);
        int length = glGetProgrami(program, GL_INFO_LOG_LENGTH);
        System.out.println("E5f=" + glGetError());
        if (length > 0) {
            String log = glGetProgramInfoLog(program);
            LOGGER.error(log);
            if (status != GL_TRUE) {
                throw new RuntimeException("Shader failed to compile, see log");
            }
        }
        glDeleteShader(vert);
        glDeleteShader(frag);
        System.out.println("E5g=" + glGetError());
    }

    public static void render() {
        perfTimerIdx++;
        perfTimerIdx %= perfTimer.length;
        timer().clear();
        timer().beginFrame = System.nanoTime();
        glStateSetup();
        matrixSetup();
        resetBuffer();

        drawSelectedWorld(bufferBuilder);

        sortQuads();
        Pair<Integer, FloatBuffer> renderBuffer = quadsToTriangles();
        setBufferData(renderBuffer);
        preDrawSetup();
        shaderUniforms();
        drawBuffer(renderBuffer);
        freeBuffer(renderBuffer);
        glFinish();

        timer().glFinish = System.nanoTime();

        drawPerfStats();
        glfwSwapBuffers(window);
    }

    private static void glStateSetup() {
        glClearColor(0.1f, 0.1f, 0.9f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);

        glUseProgram(shaderProgram);

        timer().glStateSetup = System.nanoTime();
    }

    private static void matrixSetup() {
        mvpMatrix.setIdentity();
        // mvp = projection*view*model
        // projection
        mvpMatrix.multiply(Matrix4f.perspective(60, screenWidth / screenHeight, 0.01f, 1000));
        Matrix4f modelView = inverseMatrix;
        modelView.setIdentity();
        // view
        modelView.multiply(Matrix4f.createTranslateMatrix(0, 0, -500));
        // model
        if (!HEIGHTMAP_VIEW_ENABLED) {
            modelView.multiply(Vector3f.XP.rotationDegrees(30));
            modelView.multiply(Vector3f.YP.rotationDegrees((float) ((System.currentTimeMillis() * 0.04) % 360)));
        }

        mvpMatrix.multiply(modelView);
        inverseMatrix.invert();
        timer().matrixSetup = System.nanoTime();
    }

    private static void resetBuffer() {
        if (bufferBuilder.building()) {
            bufferBuilder.end();
        }
        bufferBuilder.discard();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        timer().bufferReset = System.nanoTime();
    }

    private static void drawSelectedWorld(BufferBuilder builder) {
        Level w = serverWorlds.get(LevelStem.OVERWORLD);
        if (w == null) {
            return;
        }

        drawWorld(builder, w);
    }

    private static void drawWorld(BufferBuilder builder, Level world) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        int playerX = player == null ? 0 : Coords.getCubeXForEntity(player);
        int playerY = player == null ? 0 : Coords.getCubeYForEntity(player);
        int playerZ = player == null ? 0 : Coords.getCubeZForEntity(player);

        ChunkSource chunkProvider = world.getChunkSource();
        if (chunkProvider instanceof ServerChunkCache) {
            if (HEIGHTMAP_VIEW_ENABLED) {
                renderLightHeightmapDebug(world, player, builder);
            } else {
                Long2ByteLinkedOpenHashMap cubeMap = buildStatusMaps((ServerChunkCache) chunkProvider);
                timer().buildStatusMap = System.nanoTime();
                buildQuads(builder, playerX, playerY, playerZ, cubeMap);
                timer().buildQuads = System.nanoTime();
            }
        }

    }

    // TODO allow switching between other heightmaps too?
    private static void renderLightHeightmapDebug(Level world, AbstractClientPlayer player, BufferBuilder builder) {
        int chunkX = player == null ? 0 : Coords.blockToSection(player.getBlockX());
        int chunkZ = player == null ? 0 : Coords.blockToSection(player.getBlockZ());
        int cubeY = player == null ? 0 : Coords.blockToCube(player.getBlockY());
        int minCubeY = cubeY - 64;
        int maxCubeY = cubeY + 65;
        var chunk = world.getChunkSource().getChunkForLighting(chunkX, chunkZ);
        if (chunk == null) return;
        var lightHeightmap = ((LightHeightmapGetter) chunk).getServerLightHeightmap();

        var buffer = new ArrayList<Vertex>();

        addTreeToBuffer(buffer, lightHeightmap.getSurfaceTracker(), minCubeY, maxCubeY);

        for (Vertex v : buffer) {
            vertex(builder, v.x, v.y, v.z, v.nx, v.ny, v.nz, v.rgba);
        }
    }

    private static void addTreeToBuffer(List<Vertex> buffer, SurfaceTrackerSection node, int minCubeY, int maxCubeY) {
        int scale = node.getScale();
        int scaledY = node.getScaledY();
        int bottomCubeY = SurfaceTrackerSection.scaledYBottomY(scaledY, scale);
        int topCubeY = bottomCubeY + (1 << (scale * SurfaceTrackerSection.NODE_COUNT_BITS));
        minCubeY = Math.max(minCubeY, bottomCubeY);
        maxCubeY = Math.min(maxCubeY, topCubeY);
        if (minCubeY >= maxCubeY) return;
        // TODO should be checking whether scale0 sections have their cubes loaded
        var isDirectlyLoaded = (scale == 0);
        if (scale != 0) {
            for (int i = 0; i < SurfaceTrackerSection.NODE_COUNT; i++) {
                var child = node.getChild(i);
                if (child != null) {
                    isDirectlyLoaded = true;
                    addTreeToBuffer(buffer, child, minCubeY, maxCubeY);
                }
            }
        }
        int color = isDirectlyLoaded ? 0xFFFFFFFF : 0xFF77FF77;
        if ((scaledY & 1) == 1) color = darken(color, 15);
        if ((scale & 1) == 1) color = darken(color, 8);
        float x0 = -scale;
        float x1 = x0 + 1;
        float y0 = minCubeY;
        float y1 = maxCubeY;
        float z0 = 0;
        x0 *= 7; x1 *= 7; y0 *= 7; y1 *= 7;
        buffer.add(new Vertex(x0, y1, z0, 0, 0, -1, color));
        buffer.add(new Vertex(x1, y1, z0, 0, 0, -1, color));
        buffer.add(new Vertex(x1, y0, z0, 0, 0, -1, color));
        buffer.add(new Vertex(x0, y0, z0, 0, 0, -1, color));
    }

    private static Long2ByteLinkedOpenHashMap buildStatusMaps(ServerChunkCache chunkProvider) {
        ChunkMap chunkManager = chunkProvider.chunkMap;
        Long2ObjectLinkedOpenHashMap<ChunkHolder> loadedCubes = getField(ChunkMap.class, chunkManager, "visibleCubeMap");

        Object[] data = getField(Long2ObjectLinkedOpenHashMap.class, loadedCubes, "value");
        long[] keys = getField(Long2ObjectLinkedOpenHashMap.class, loadedCubes, "key");
        Long2ByteLinkedOpenHashMap cubeMap = new Long2ByteLinkedOpenHashMap(100000);
        for (int i = 0, keysLength = keys.length; i < keysLength; i++) {
            long pos = keys[i];
            if (pos == 0) {
                continue;
            }
            ChunkHolder holder = (ChunkHolder) data[i];
            ChunkStatus status = holder == null ? null : ICubeHolder.getCubeStatusFromLevel(holder.getTicketLevel());
            ChunkAccess chunk = holder == null ? null : holder.getChunkToSave().getNow(null);
            ChunkStatus realStatus = chunk == null ? null : chunk.getStatus();
            cubeMap.put(pos, realStatus == null ? (byte) 255 : (byte) Registry.CHUNK_STATUS.getId(realStatus));
        }
        return cubeMap;
    }

    private static <T> T getField(Class<?> cl, Object obj, String name) {
        try {
            Field f = cl.getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildQuads(BufferBuilder builder, int playerX, int playerY, int playerZ, Long2ByteLinkedOpenHashMap cubeMap) {
        Object2IntMap<ChunkStatus> colors = getField(
            LevelLoadingScreen.class, null, "COLORS" // TODO: intermediary name
        );
        int[] colorsArray = new int[256];
        ChunkStatus[] statusLookup = new ChunkStatus[256];
        for (ChunkStatus chunkStatus : colors.keySet()) {
            int id = Registry.CHUNK_STATUS.getId(chunkStatus);
            colorsArray[id] = colors.get(chunkStatus);
            statusLookup[id] = chunkStatus;
        }
        colorsArray[255] = 0x00FF00FF;

        Direction[] directions = Direction.values();
        float ratioFactor = 1 / (float) ChunkStatus.FULL.getIndex();
        final boolean drawNull = false;

        Map<ChunkStatus, List<Vertex>> verts = new HashMap<>();
        for (ChunkStatus chunkStatus : ChunkStatus.getStatusList()) {
            verts.put(chunkStatus, new ArrayList<>());
        }
        for (Long2ByteMap.Entry e : cubeMap.long2ByteEntrySet()) {
            long posLong = e.getLongKey();
            int posX = CubePos.extractX(posLong);
            int posY = CubePos.extractY(posLong);
            int posZ = CubePos.extractZ(posLong);
            int status = e.getByteValue() & 0xFF;
            if (!drawNull && status == 255) {
                continue;
            }
            ChunkStatus statusObj = statusLookup[status];
            float ratio = statusObj == null ? 1 : statusObj.getIndex() * ratioFactor;
            int alpha = status == 255 ? 0x22 : (int) (0x20 + ratio * (0xFF - 0x20));
            int c = colorsArray[status] | (alpha << 24);

            EnumSet<Direction> renderFaces = findRenderFaces(cubeMap, directions, drawNull, posX, posY, posZ, status);

            List<Vertex> buffer = verts.get(statusObj);
            if (buffer != null) {
                drawCube(buffer, posX - playerX, posY - playerY, posZ - playerZ, 7, c, renderFaces);
            }
        }
        buildVertices(builder, verts);
    }

    private static EnumSet<Direction> findRenderFaces(Long2ByteLinkedOpenHashMap cubeMap, Direction[] directions, boolean drawNull, int posX, int posY, int posZ, int status) {
        EnumSet<Direction> renderFaces = EnumSet.noneOf(Direction.class);
        for (Direction value : directions) {
            long l = CubePos.asLong(posX + value.getStepX(), posY + value.getStepY(), posZ + value.getStepZ());
            int cubeStatus = cubeMap.get(l) & 0xFF;
            // this.ordinal() >= status.ordinal();
            if (drawNull) {
                if (status == 255 || cubeStatus == 255 || cubeStatus < status) {
                    renderFaces.add(value);
                }
            } else {
                if (status != 255 && (cubeStatus == 255 || cubeStatus < status)) {
                    renderFaces.add(value);
                }
            }
        }
        return renderFaces;
    }

    private static void buildVertices(BufferBuilder builder, Map<ChunkStatus, List<Vertex>> verts) {
        List<ChunkStatus> statusList = ChunkStatus.getStatusList();
        for (int i = statusList.size() - 1; i >= 0; i--) {
            ChunkStatus chunkStatus = statusList.get(i);
            for (Vertex v : verts.get(chunkStatus)) {
                vertex(builder, v.x, v.y, v.z, v.nx, v.ny, v.nz, v.rgba);
            }
        }
    }

    private static void sortQuads() {
        Vector4f vec = new Vector4f(0, 0, 0, 1);
        vec.transform(inverseMatrix);
        //bufferBuilder.setQuadSortOrigin(vec.x(), vec.y(), vec.z());

        bufferBuilder.end();
        timer().sortQuads = System.nanoTime();
    }

    private static Pair<Integer, FloatBuffer> quadsToTriangles() {
        Pair<BufferBuilder.DrawState, ByteBuffer> stateBuffer = bufferBuilder.popNextBuffer();
        stateBuffer.getSecond().order(ByteOrder.nativeOrder());
        Pair<Integer, FloatBuffer> integerFloatBufferPair = toTriangles(stateBuffer);
        timer().toTriangles = System.nanoTime();
        return integerFloatBufferPair;
    }

    private static Pair<Integer, FloatBuffer> toTriangles(Pair<BufferBuilder.DrawState, ByteBuffer> stateBuffer) {
        FloatBuffer in = stateBuffer.getSecond().asFloatBuffer();
        in.clear();
        int quadCount = stateBuffer.getFirst().vertexCount() / 4;
        int triangleCount = quadCount * 2;
        int floatsPerVertex = stateBuffer.getFirst().format().getIntegerSize();
        ByteBuffer outBytes = MemoryUtil.memAlloc(Float.BYTES * triangleCount * 3 * floatsPerVertex);
        outBytes.order(ByteOrder.nativeOrder());
        FloatBuffer out = outBytes.asFloatBuffer();
        for (int i = 0; i < quadCount; i++) {
            int startPos = i * 4 * floatsPerVertex;
            int endPos = startPos + floatsPerVertex * 3;
            in.limit(endPos);
            in.position(startPos);
            out.put(in);

            in.limit(startPos + floatsPerVertex);
            in.position(startPos);
            out.put(in);

            in.limit(endPos);
            in.position(endPos - floatsPerVertex);
            out.put(in);

            in.limit(endPos + floatsPerVertex);
            in.position(endPos);
            out.put(in);
        }
        out.clear();
        return new Pair<>(triangleCount * 3, out);
    }

    private static void setBufferData(Pair<Integer, FloatBuffer> renderBuffer) {
        glBufferData(GL_ARRAY_BUFFER, renderBuffer.getSecond(), GL_STREAM_DRAW);
        timer().setBufferData = System.nanoTime();
    }

    private static void preDrawSetup() {
        glEnableVertexAttribArray(posAttrib);
        glEnableVertexAttribArray(colAttrib);

        // 12 bytes per float pos + 4 bytes per color = 16 bytes
        glVertexAttribPointer(posAttrib, 3, GL_FLOAT, false, 16, 0);
        glVertexAttribPointer(colAttrib, 4, GL_UNSIGNED_BYTE, true, 16, 12);

        timer().preDrawSetup = System.nanoTime();
    }

    private static void shaderUniforms() {
        ByteBuffer byteBuffer = MemoryUtil.memAlignedAlloc(64, 64);
        FloatBuffer fb = byteBuffer.asFloatBuffer();
        mvpMatrix.store(fb);
        glUniformMatrix4fv(matrixLocation, false, fb);
        MemoryUtil.memFree(byteBuffer);
    }

    private static void drawBuffer(Pair<Integer, FloatBuffer> renderBuffer) {
        glDrawArrays(GL_TRIANGLES, 0, renderBuffer.getFirst());
        timer().draw = System.nanoTime();
    }

    private static void freeBuffer(Pair<Integer, FloatBuffer> renderBuffer) {
        MemoryUtil.memFree(renderBuffer.getSecond());
        timer().freeMem = System.nanoTime();
    }


    private static void drawPerfStats() {
        if (perfGraphBuilder.building()) {
            perfGraphBuilder.end();
        }
        perfGraphBuilder.discard();

        int[] colors = {
            0x000000, // glStateSetup
            0xFFFFFF, // matrixSetup
            0xFF0000, // bufferReset
            0x00FF00, // buildStatusMap
            0xFFFF00, // buildQuads
            0xFF00FF, // sortQuads
            0xC0C0C0, // toTriangles
            0x808080, // setBufferData
            0x800000, // preDrawSetup
            0x808000, // draw
            0x008000, // freeMem
            0x800080, // glFinish
            0x8B4513, //
            0x708090, //
            0x8FBC8F, //
            0x808000, //
            0xB8860B
        };
        perfGraphBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < perfTimer.length; i++) {
            int x = perfTimer.length - 1 - i;
            PerfTimer timer = perfTimer[(i + perfTimerIdx) % perfTimer.length];
            if (timer == null) {
                continue;
            }
            AtomicInteger ci = new AtomicInteger();
            timer.drawTimer((yStart, yEnd) -> {
                int cx = ci.getAndIncrement();
                int col = (cx >= colors.length ? 0 : colors[cx]) | 0xFF000000;
                quad2d(perfGraphBuilder, x * 3, yStart, x * 3 + 3, yEnd, col);
            });
        }

        perfGraphBuilder.end();

        glUseProgram(shaderProgram);

        Matrix4f ortho = MathUtil.createMatrix(new float[] {
            2f / 854f, 0, 0, -1f,
            0, 2f / 480f, 0, -1f,
            0, 0, -2f / 2000f, 0,
            0, 0, 0, 1
        });

        FloatBuffer buffer = MemoryUtil.memAllocFloat(16);
        ortho.store(buffer);
        buffer.clear();
        glUniformMatrix4fv(matrixLocation, false, buffer);
        MemoryUtil.memFree(buffer);


        Pair<BufferBuilder.DrawState, ByteBuffer> nextBuffer = perfGraphBuilder.popNextBuffer();
        nextBuffer.getSecond().clear();
        Pair<Integer, FloatBuffer> buf = toTriangles(nextBuffer);
        buf.getSecond().clear();
        glBufferData(GL_ARRAY_BUFFER, buf.getSecond(), GL_STREAM_DRAW);
        preDrawSetup();
        glDrawArrays(GL_TRIANGLES, 0, buf.getFirst());
        MemoryUtil.memFree(buf.getSecond());
    }

    private static void quad2d(BufferBuilder buf, float x1, float y1, float x2, float y2, int color) {
        vertex(buf, x1, y1, 1, 0, 0, 0, color);
        vertex(buf, x1, y2, 1, 0, 0, 0, color);
        vertex(buf, x2, y2, 1, 0, 0, 0, color);
        vertex(buf, x2, y1, 1, 0, 0, 0, color);
    }

    private static void drawCube(List<Vertex> buffer, int x, int y, int z, float scale, int color, EnumSet<Direction> renderFaces) {
        float x0 = x * scale;
        float x1 = x0 + scale;
        float y0 = y * scale;
        float y1 = y0 + scale;
        float z0 = z * scale;
        float z1 = z0 + scale;
        if (renderFaces.contains(Direction.UP)) {
            // up face
            buffer.add(new Vertex(x0, y1, z0, 0, 1, 0, color));
            buffer.add(new Vertex(x0, y1, z1, 0, 1, 0, color));
            buffer.add(new Vertex(x1, y1, z1, 0, 1, 0, color));
            buffer.add(new Vertex(x1, y1, z0, 0, 1, 0, color));
        }
        if (renderFaces.contains(Direction.DOWN)) {
            int c = darken(color, 40);
            // down face
            buffer.add(new Vertex(x0, y0, z0, 0, 1, 0, color));
            buffer.add(new Vertex(x0, y0, z1, 0, 1, 0, color));
            buffer.add(new Vertex(x1, y0, z1, 0, 1, 0, color));
            buffer.add(new Vertex(x1, y0, z0, 0, 1, 0, color));
        }
        if (renderFaces.contains(Direction.EAST)) {
            int c = darken(color, 30);
            // right face
            buffer.add(new Vertex(x1, y1, z0, 1, 0, 0, c));
            buffer.add(new Vertex(x1, y1, z1, 1, 0, 0, c));
            buffer.add(new Vertex(x1, y0, z1, 1, 0, 0, c));
            buffer.add(new Vertex(x1, y0, z0, 1, 0, 0, c));
        }
        if (renderFaces.contains(Direction.WEST)) {
            int c = darken(color, 30);
            // left face
            buffer.add(new Vertex(x0, y1, z0, 1, 0, 0, c));
            buffer.add(new Vertex(x0, y1, z1, 1, 0, 0, c));
            buffer.add(new Vertex(x0, y0, z1, 1, 0, 0, c));
            buffer.add(new Vertex(x0, y0, z0, 1, 0, 0, c));
        }
        if (renderFaces.contains(Direction.NORTH)) {
            int c = darken(color, 20);
            // front face (facing camera)
            buffer.add(new Vertex(x0, y1, z0, 0, 0, -1, c));
            buffer.add(new Vertex(x1, y1, z0, 0, 0, -1, c));
            buffer.add(new Vertex(x1, y0, z0, 0, 0, -1, c));
            buffer.add(new Vertex(x0, y0, z0, 0, 0, -1, c));
        }
        if (renderFaces.contains(Direction.SOUTH)) {
            int c = darken(color, 20);
            // back face
            buffer.add(new Vertex(x0, y1, z1, 0, 0, -1, c));
            buffer.add(new Vertex(x1, y1, z1, 0, 0, -1, c));
            buffer.add(new Vertex(x1, y0, z1, 0, 0, -1, c));
            buffer.add(new Vertex(x0, y0, z1, 0, 0, -1, c));
        }
    }

    private static int darken(int color, int amount) {
        int r = color >>> 16 & 0xFF;
        r -= (r * amount) / 100;
        int g = color >>> 8 & 0xFF;
        g -= (g * amount) / 100;
        int b = color & 0xFF;
        b -= (b * amount) / 100;
        return color & 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static void vertex(BufferBuilder buffer, float x, float y, float z, int nx, int ny, int nz, int color) {
        // color = (color & 0xFF000000) | ((~color) & 0x00FFFFFF);
        int r = color >>> 16 & 0xFF;
        int g = color >>> 8 & 0xFF;
        int b = color & 0xFF;
        int a = color >>> 24;

        buffer.vertex(x, y, z);
        buffer.color(r, g, b, a);
        buffer.endVertex();
    }

    static class Vertex {
        float x, y, z;
        int nx, ny, nz;
        int rgba;

        Vertex(float x, float y, float z, int nx, int ny, int nz, int rgba) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
            this.rgba = rgba;
        }
    }

    public static class PerfTimer {

        private long beginFrame;
        private long glStateSetup;
        private long matrixSetup;
        private long bufferReset;
        private long buildStatusMap;
        private long buildQuads;
        private long sortQuads;
        private long toTriangles;
        private long setBufferData;
        private long preDrawSetup;
        private long draw;
        private long freeMem;
        private long glFinish;

        public void clear() {
            beginFrame = 0;
            glStateSetup = 0;
            matrixSetup = 0;
            bufferReset = 0;
            buildStatusMap = 0;
            buildQuads = 0;
            sortQuads = 0;
            toTriangles = 0;
            setBufferData = 0;
            preDrawSetup = 0;
            draw = 0;
            freeMem = 0;
            glFinish = 0;
        }

        public void drawTimer(FloatBiConsumer line) {
            double scale = 0.3f / TimeUnit.MILLISECONDS.toNanos(1);


            double glFinishTime = this.glFinish;
            double freeMemTime = this.freeMem;
            double drawTime = this.draw;
            double preDrawSetupTime = this.preDrawSetup;
            double setBufferDataTime = this.setBufferData;
            double toTrianglesTime = this.toTriangles;
            double sortQuadsTime = this.sortQuads;
            double buildQuadsTime = this.buildQuads;
            double buildStatusMapTime = this.buildStatusMap;
            double bufferResetTime = this.bufferReset;
            double matrixSetupTime = this.matrixSetup;
            double glStateSetupTime = this.glStateSetup;

            glFinishTime -= freeMem;
            freeMemTime -= draw;
            drawTime -= preDrawSetup;
            preDrawSetupTime -= setBufferData;
            setBufferDataTime -= toTriangles;
            toTrianglesTime -= sortQuads;
            sortQuadsTime -= buildQuads;
            buildQuadsTime -= buildStatusMap;
            buildStatusMapTime -= bufferReset;
            bufferResetTime -= matrixSetup;
            matrixSetupTime -= glStateSetup;
            glStateSetupTime -= beginFrame;

            float y = 0;
            line.accept(y, y += (glStateSetupTime * scale));
            line.accept(y, y += (matrixSetupTime * scale));
            line.accept(y, y += (bufferResetTime * scale));
            line.accept(y, y += (buildStatusMapTime * scale));
            line.accept(y, y += (buildQuadsTime * scale));
            line.accept(y, y += (sortQuadsTime * scale));
            line.accept(y, y += (toTrianglesTime * scale));
            line.accept(y, y += (setBufferDataTime * scale));
            line.accept(y, y += (preDrawSetupTime * scale));
            line.accept(y, y += (drawTime * scale));
            line.accept(y, y += (freeMemTime * scale));
            line.accept(y, y += (glFinishTime * scale));
        }
    }

    @FunctionalInterface
    public interface FloatBiConsumer {

        void accept(float a, float b);
    }
}
