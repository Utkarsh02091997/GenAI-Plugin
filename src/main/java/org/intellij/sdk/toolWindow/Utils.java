package org.intellij.sdk.toolWindow;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import net.minidev.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Utils {

    public String selectedFilePath;
    private List<String> allProjectFileList = new ArrayList<>();
    private openAiClient openAiClient = new openAiClient();


    public void selectAndReadFileFromComboBox(String selectedFileName) {
        selectedFilePath = getFilePathByName(selectedFileName);
        System.out.println("selectedFilePath = " + selectedFilePath);

    }

    private String getFilePathByName(String fileName) {
        for (String filePath : allProjectFileList) {
            if (getFileName(filePath).equals(fileName)) {
                return filePath;
            }
        }
        return null;
    }

    public void populateFileList(JComboBox<String> fileListComboBox, Project project) {
        allProjectFileList = getProjectFileList(project);
        Collections.sort(allProjectFileList, (filePath1, filePath2) -> {
            String fileName1 = getFileName(filePath1);
            String fileName2 = getFileName(filePath2);
            return fileName1.compareTo(fileName2);
        });
        for (String filePath : allProjectFileList) {
            String fileName = getFileName(filePath);
            fileListComboBox.addItem(fileName);
        }
    }

    private String getFileName(String filePath) {
// Extract and return the file name from the file path
        int lastSeparatorIndex = filePath.lastIndexOf('/');
        return filePath.substring(lastSeparatorIndex + 1);
    }

    public List<String> getProjectFileList(Project project) {
        List<String> fileList = new ArrayList<>();

        VirtualFile baseDir = project.getBaseDir();
        if (baseDir != null) {
            VfsUtilCore.visitChildrenRecursively(baseDir, new VirtualFileVisitor() {
                @Override
                public boolean visitFile(@SuppressWarnings("NullableProblems") VirtualFile file) {
                    // Check if the file is not a directory
                    if (!file.isDirectory()) {
                        String filePath = file.getPath();

                        // Filter out files with specific extensions
                        if (filePath.endsWith(".java") ||
                                filePath.endsWith(".jsp") ||
                                filePath.endsWith(".xml") ||
                                filePath.endsWith(".jws") ||
                                filePath.endsWith(".properties")) {

                            // Filter out files in specific folders
                            String[] ignoredFolders = {"/target/", "/.idea/", "/.gradle/"};
                            boolean ignoreFile = false;

                            for (String folder : ignoredFolders) {
                                if (filePath.contains(folder)) {
                                    ignoreFile = true;
                                    break;
                                }
                            }

                            // Filter out hidden files/folders
                            if (!filePath.contains("/.")) {
                                if (!ignoreFile) {
                                    fileList.add(filePath);
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        }
        return fileList;
    }

    private String getFileContent(Editor editor) {
        if (editor != null) {
            return editor.getDocument().getText();
        }
        return "";
    }

    public String generateJsonData(String userInput) {
        JSONObject jsonInput = new JSONObject();
        jsonInput.put("userInput", userInput);

        String jsonData = jsonInput.toString();
        return jsonData;
    }

    public String callEndPoint(String systemMessage, String userMessage) throws Exception {

        String result = openAiClient.getPromptOrTag(systemMessage, userMessage);

        return result;

    }

    public void findSummary(String userInput, JTextArea textArea) {
        String systemMessage = "Summarise the main points of the article in a list format:\n";

        String result = null;
        try {
            result = callEndPoint(systemMessage, userInput);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        textArea.setText(result);
    }

    public String readFileFromProject() {
        if (Objects.isNull(selectedFilePath)) //first index
            selectedFilePath = allProjectFileList.get(0);
        Path path = Paths.get(selectedFilePath);
        String fileContents = null;
        try {
            fileContents = Files.lines(path)
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("File Contents:\n" + fileContents);
        return fileContents;
    }

    public void migrateFileToSpringBoot(String userInput, JTextArea textArea, String oldTech,String migrationOption) {
//        String user_prompt_version_update = " \"\"\" I have provided the file data below delimited by ``` :\n" +
//                " \n" +
//                "```\n" +
//                userInput + "\n" +
//                "\n" +
//                "```\n" +
//                "\n" +
//                "Strictly adhere to the instructions and make changes acordingly\n" +
//                "1. Convert the file's version to the latest Spring boot version 3.1 \n" +
//                "2. Conversion should maintain all dependencies, plugins, and properties from the original input provided.\n" +
//                "3. Strictly provide the code , do not provide any explanation.\n" +
//                "4. Add necessary dependencies while changing the version.\n" +
//                "5. Do not change the functionality of the code in any way.\n" +
//                "6.Give the response without triple ` \n" +
//                "\n" +
//                "\"\"\"\n";



        String systemPrompt="You are a seasoned software engineer working on a project migration tool. Your task is to assist users in migrating individual files from older technologies (such as Struts, iBatis, Axis, JDBC) to the latest technologies (such as Spring MVC, Spring Boot, JPA, Spring MVC Thymeleaf ). Users will provide specific files and migration preferences. Understand the old technologies specific file (class, configuration) selected or piece of code passed and convert it to latest technology selected by user to its corresponding file or code .";
        String userPrompt="1. **Old Technology:** "+oldTech +
                "2. **File or Code to Migrate:** "+userInput +
                "3. **Migration Options:** "+migrationOption +
                " \n" +
                "Guidelines for Migration: \n" +
                "Guidelines for Migration:\n" +
                "- Double-check the file path and name to ensure accuracy.\n" +
                "- Select appropriate migration options based on the old technology.\n" +
                "- Provide clear additional configurations if required (optional).\n" +
                "- Ensure a backup of the original file is available before migration.\n" +
                "- Follow best practices of the target technology during migration.\n" +
                "- If uncertain, ask for clarifications before proceeding.\n" +
                " \n" +
                "Please provide accurate and detailed information for successful migration.\n";
        String result = null;
        try {

                result = callEndPoint(systemPrompt, userInput);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        textArea.setText(result);


    }

    public String testCaseGeneration(String userInput, Boolean isFromFile) {
//        String system_prompt_gen_test_cases_template = "Generate the Junit test cases for the code given by the user.\n" +
//                "\n" +
//                "Strictly adhere to these guidelines while generating test case:\n" +
//                " \n" +
//                "1.Include all the import statement needed.\n" +
//                "2. Use @MockBean where ever is required and If any annotation used in file import the package from which annotation is coming.\n" +
//                "3. Import org.junit.jupiter.api.Assertions.* in all the testcase classes.\n" +
//                "4. Analyse the first method for which you are writing test case will now correctly mock the behavior of both findById and save methods of the userRepository. 5.Generate the delete test-case with extra analysis and Do not forgot to mock any dependent call.\n" +
//                "6. EditUser testcase should generate with extra analysis should not throw the exception while running. \n" +
//                "7. While writing testcase for Controller do the proper analysis and follow the POST and PUT method like example provided below and do not remove \\ from content json.\n" +
//                "8. The output should be a valid testcase and strictly do not include any additional text other than test case. Do not provide any explanations or additional text, only provide the test case code.\n" +
//                "\n" +
//                "example:\n" +
//                "\n" +
//                "@Test\n" +
//                "public void editUserTest() {\n" +
//                "User user = new User();\n" +
//                "user.setId(1L);\n" +
//                "user.setUsername(\"test\");\n" +
//                "user.setPassword(\"password\");\n" +
//                "when(userRepository.findById(1L)).thenReturn(Optional.of(user));\n" +
//                "when(userRepository.save(user)).thenReturn(user);\n" +
//                "assertNotNull(userService.editUser(1L, user));\n" +
//                "}\n" +
//                "\n" +
//                "Put this data in ``` and and your response should solely consist of the testcase and should not include any additional text. \n" +
//                "\n ";

        String systemPrompt = "You are a senior software engineer responsible for maintaining code quality and reliability in a Spring-based web application. Your task is writing unit tests for controller classes in the application. The controllers handle requests in a Spring MVC, Spring Boot, or REST API context. Provide detailed instructions and ask clarifying questions to ensure accurate and effective unit testing.";

        String userPromptSuffix = "Note: Provide clear and detailed information for effective unit testing. Follow the guidelines below for writing comprehensive tests.\n" +
                " \n" +
                "Guidelines for Writing Unit Tests for Controller Classes:\n" +
                "- Choose an appropriate testing framework (e.g., JUnit, TestNG) for writing unit tests.\n" +
                "- Specify the mocking framework (e.g., Mockito) if mocking is required for dependencies.\n" +
                "- Provide any additional dependencies required for testing, if applicable.\n" +
                "- Write comprehensive unit tests to cover different scenarios and edge cases.\n" +
                "- Ensure tests are isolated, independent, and can be run in any order.\n" +
                "- Follow naming conventions for test methods (e.g., testMethodName_shouldReturnExpectedResult).\n" +
                "- Provide meaningful assertions to validate the behavior of the controller methods.\n" +
                "- Include positive and negative test cases to validate both expected and unexpected behavior.\n" +
                "- Properly handle exceptions and edge cases in the test scenarios.\n" +
                "- Provide clear and concise explanations in comments for complex test scenarios.\n" +
                "- Test asynchronous and concurrent behavior if applicable.\n" +
                "- Document the purpose of each test case for future reference.\n" +
                " \n" +
                "Please provide accurate and detailed information for writing effective unit tests for the specified controller class and methods.";

        String userInputText;
        if (isFromFile) {
            userInputText = "1. **Controller Class:**'''" + userInput + "'''\n" +
                    "2. **Testing Framework:** (Select the testing framework from,  Junit or TestNG)\n" +
                    "3. **Mocking Framework:** Mockito";
        } else {
            userInputText = "1. **Methods to Test:**'''" + userInput + "'''\n" +
                    "2. **Testing Framework:** (Select the testing framework from,  Junit or TestNG)\n" +
                    "3. **Mocking Framework:** Mockito";
        }
        String finalUserPrompt = userInputText + "\n\n\n" + userPromptSuffix;
        System.out.println(finalUserPrompt);

        String result = null;
        try {
            result = callEndPoint(systemPrompt, finalUserPrompt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;


    }

    public String convertIntoHtml(String userInput) {

        String system_prompt_html_template = "Generate the html code for the provided text. Decorate as much as you can. provide the code without triple ` and any explanation.\n" +
                "Remove tripple backtick if there.Only return the code\n";

        String result = null;
        try {
            result = callEndPoint(system_prompt_html_template, userInput);
            Document document = Jsoup.parse(result);
// // Get the final HTML as a string
            return document.html();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}