package ui;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.util.ArrayDeque;
import java.util.Deque;

public class UserInterfaceRenderer {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private static final int MEDIAN_FILTER_SIZE = 10;
    private final Deque<Float> fpsValues = new ArrayDeque<>(MEDIAN_FILTER_SIZE);

    public UserInterfaceRenderer(long window) {

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();

        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        imGuiGlfw.init(window, true);
        imGuiGl3.init("#version 150");
    }

    public void render(float delta) {

        fpsValues.add(delta);
        if (fpsValues.size() > MEDIAN_FILTER_SIZE) {
            fpsValues.poll();
        }

        imGuiGlfw.newFrame();
        imGuiGl3.newFrame();
        ImGui.newFrame();
        ImGui.begin("Cubed");

        double averageDeltaTime = fpsValues.stream().reduce(0f, Float::sum) / MEDIAN_FILTER_SIZE;

        ImGui.text(String.format("%f frame time", averageDeltaTime * 1000f));
        ImGui.text(String.format("%f fps", 1.0 / averageDeltaTime));

        ImGui.end();
        ImGui.render();

        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void shutdown() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }
}
