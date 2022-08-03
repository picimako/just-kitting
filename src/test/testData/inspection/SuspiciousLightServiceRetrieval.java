import com.intellij.openapi.components.Service;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.module.Module;

public class SuspiciousLightServiceRetrieval {
    public void highlightCases(Project project) {
        ApplicationManager.getApplication().getService(<weak_warning descr="Referenced class is marked as project service (@Service.Level.PROJECT) but is being retrieved as an application service.">SomeProjectService</weak_warning>.class);
        project.getService(<weak_warning descr="Referenced class is marked as application service (@Service.Level.APP) but is being retrieved as a project service.">SomeApplicationService</weak_warning>.class);

        ApplicationManager.getApplication().getServiceIfCreated(<weak_warning descr="Referenced class is marked as project service (@Service.Level.PROJECT) but is being retrieved as an application service.">SomeProjectService</weak_warning>.class);
        project.getServiceIfCreated(<weak_warning descr="Referenced class is marked as application service (@Service.Level.APP) but is being retrieved as a project service.">SomeApplicationService</weak_warning>.class);
    }

    public void noHighlightCases(Project project, Module module) {
        ApplicationManager.getApplication().getService(SomeApplicationService.class);
        project.getService(SomeProjectService.class);
        module.getService(SomeApplicationService.class);
        module.getService(SomeProjectService.class);
        ApplicationManager.getApplication().getService(SomeProjectAndApplicationService.class);
        project.getService(SomeProjectAndApplicationService.class);
        module.getService(SomeProjectAndApplicationService.class);
        ApplicationManager.getApplication().getService(SomeLightService.class);
        project.getService(SomeLightService.class);
        module.getService(SomeLightService.class);

        ApplicationManager.getApplication().getServiceIfCreated(SomeApplicationService.class);
        project.getServiceIfCreated(SomeProjectService.class);
        ApplicationManager.getApplication().getServiceIfCreated(SomeProjectAndApplicationService.class);
        project.getServiceIfCreated(SomeProjectAndApplicationService.class);
        ApplicationManager.getApplication().getServiceIfCreated(SomeLightService.class);
        project.getServiceIfCreated(SomeLightService.class);
    }

    @Service(Service.Level.PROJECT)
    private static final class SomeProjectService {
    }

    @Service(Service.Level.APP)
    private static final class SomeApplicationService {
    }

    @Service({Service.Level.APP, Service.Level.PROJECT})
    private static final class SomeProjectAndApplicationService {
    }

    @Service
    private static final class SomeLightService {
    }
}