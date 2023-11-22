import java.util.Date;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.toedter.calendar.JCalendar;

public class ToDoListApp {

    private JFrame frame;
    private DefaultListModel<Task> taskListModel;
    private JList<Task> taskList;
    private JTextField taskInput;
    private JTextField searchInput;
    private JList<String> categoryList;
    private JButton addButton;
    private JButton deleteButton;
    private JButton speakButton;
    private JButton selectCategoryButton;
    private JButton selectDateButton;
    private JButton searchButton;
    private Voice voice;
    private String selectedCategory = "";
    private String selectedDate = "No Due Date";
    private JCalendar calendar;

    public ToDoListApp() {
        initialize();

        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        voice = VoiceManager.getInstance().getVoice("kevin16");
        if (voice != null) {
            voice.allocate();
        }
    }

    private void initialize() {
        frame = new JFrame("To-Do List");
        frame.setBounds(100, 100, 1000, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.BLACK);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        frame.add(rightPanel, BorderLayout.EAST);

        calendar = new JCalendar();
        calendar.addPropertyChangeListener("date", evt -> updateTaskListForSelectedDate());
        rightPanel.add(calendar, BorderLayout.CENTER);

        JPanel leftSidebar = new JPanel();
        leftSidebar.setLayout(new BorderLayout());
        frame.add(leftSidebar, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchInput = new JTextField();
        searchInput.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        searchButton = new JButton(new ImageIcon("search_icon.png"));
        searchButton.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchTasks();
            }
        });
        searchPanel.add(searchInput, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        leftSidebar.add(searchPanel, BorderLayout.NORTH);

        String[] categories = { "Today's Tasks", "Important", "Groceries", "All Tasks" };
        categoryList = new JList<>(categories);
        categoryList.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectCategory();
            }
        });
        categoryList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftSidebar.add(new JScrollPane(categoryList), BorderLayout.CENTER);

        deleteButton = new JButton("Delete Task");
        deleteButton.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTask();
            }
        });
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topRightPanel.add(deleteButton);
        frame.add(topRightPanel, BorderLayout.NORTH);

        speakButton = new JButton("Speak");
        speakButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speakTasks();
            }
        });
        speakButton.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        leftSidebar.add(speakButton, BorderLayout.SOUTH);

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);

        JPanel addTaskBar = new JPanel();
        addTaskBar.setLayout(new BorderLayout());
        frame.add(addTaskBar, BorderLayout.SOUTH);

        taskInput = new JTextField();
        taskInput.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        addTaskBar.add(taskInput, BorderLayout.CENTER);

        selectCategoryButton = new JButton("Select Category");
        selectCategoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectCategory();
            }
        });
        selectCategoryButton.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        addTaskBar.add(selectCategoryButton, BorderLayout.WEST);

        selectDateButton = new JButton("Date");
        selectDateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectDate();
            }
        });
        selectDateButton.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        addTaskBar.add(selectDateButton, BorderLayout.EAST);

        addButton = new JButton("Add Task");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });
        addButton.setFont(new Font("Times New Roman", Font.PLAIN, 24));
        addTaskBar.add(addButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void updateTaskListForSelectedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date selectedDate = calendar.getDate();
        String formattedDate = dateFormat.format(selectedDate);

        DefaultListModel<Task> filteredModel = new DefaultListModel<>();
        for (int i = 0; i < taskListModel.getSize(); i++) {
            Task task = taskListModel.getElementAt(i);
            try {
                Date taskDueDate = dateFormat.parse(task.getDueDate());
                String formattedTaskDueDate = dateFormat.format(taskDueDate);

                if (formattedTaskDueDate.equals(formattedDate)) {
                    filteredModel.addElement(task);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        taskList.setModel(filteredModel);
        refreshTaskList();
    }

    private void selectCategory() {
        int selectedIndex = categoryList.getSelectedIndex();
        if (selectedIndex != -1) {
            selectedCategory = categoryList.getModel().getElementAt(selectedIndex);
            refreshTaskList();
        }
    }

    private void selectDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
        String newDate = JOptionPane.showInputDialog(frame, "Enter Due Date (DD-MM-YY):", selectedDate);
        if (newDate != null) {
            try {
                dateFormat.parse(newDate);
                selectedDate = newDate;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid date format. Use DD-MM-YY.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addTask() {
        String taskDescription = taskInput.getText();
        if (!taskDescription.isEmpty() && !selectedCategory.isEmpty()) {
            Task newTask = new Task(taskDescription, selectedCategory, selectedDate);
            taskListModel.addElement(newTask);
            taskInput.setText("");
            refreshTaskList();
        }
    }

    private void speakTasks() {
        if (voice != null) {
            ListModel<Task> displayedTasks = taskList.getModel();
            for (int i = 0; i < displayedTasks.getSize(); i++) {
                Task task = displayedTasks.getElementAt(i);
                String taskText = task.getDescription();
                String dueDate = task.getDueDate();
                if (!"No Due Date".equals(dueDate)) {
                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yy");
                        SimpleDateFormat outputFormat = new SimpleDateFormat("dd, MMMM, yyyy");
                        Date date = inputFormat.parse(dueDate);
                        taskText += " due on " + outputFormat.format(date);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                voice.speak(taskText);
            }
        }
    }

    private void searchTasks() {
        String searchText = searchInput.getText().toLowerCase();
        DefaultListModel<Task> filteredModel = new DefaultListModel<>();
        for (int i = 0; i < taskListModel.getSize(); i++) {
            Task task = taskListModel.getElementAt(i);
            if (task.getDescription().toLowerCase().contains(searchText)) {
                filteredModel.addElement(task);
            }
        }
        taskList.setModel(filteredModel);
    }

    private void refreshTaskList() {
        DefaultListModel<Task> filteredModel = new DefaultListModel<>();
        if ("All Tasks".equals(selectedCategory)) {
            taskList.setModel(taskListModel);
        } else {
            for (int i = 0; i < taskListModel.getSize(); i++) {
                Task task = taskListModel.getElementAt(i);
                if (selectedCategory.equals(task.getCategory())) {
                    filteredModel.addElement(task);
                }
            }
            taskList.setModel(filteredModel);
        }
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            taskListModel.remove(selectedIndex);
            refreshTaskList();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ToDoListApp();
            }
        });
    }

    class Task {
        private String description;
        private String category;
        private String dueDate;

        public Task(String description, String category, String dueDate) {
            this.description = description;
            this.category = category;
            this.dueDate = dueDate;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public String getDueDate() {
            return dueDate;
        }

        @Override
        public String toString() {
            if ("All Tasks".equals(category)) {
                return description + (dueDate.equals("No Due Date") ? "" : " - Due: " + dueDate);
            } else {
                return description + " (" + category + ")"
                        + (dueDate.equals("No Due Date") ? "" : " - Due: " + dueDate);
            }
        }
    }
}
