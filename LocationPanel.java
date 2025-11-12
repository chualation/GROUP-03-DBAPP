import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LocationPanel extends JPanel{
    private final JTable table = new JTable();
    private final DefaultTableModel model = new DefaultTableModel();
    private final JTextField tfName = new JTextField();
    private final JTextField tfAreaDesc = new JTextField();
    private final JTextField tfCapacity = new JTextField("0");
    private final JComboBox<String> cbTemp = new JComboBox<>(new String[]{"None","Refrigerated","Freezer"});

    public LocationPanel(){
        setLayout(new BorderLayout(10,10));
        model.setColumnIdentifiers(new String[]{"location_id","location_name","area_description","capacity","temperature_control"});
        table.setModel(model);

        table.setDefaultEditor(Object.class, null);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.setBorder(BorderFactory.createTitledBorder("Add / Update Location"));
        form.add(new JLabel("Name:"));                 form.add(tfName);
        form.add(new JLabel("Area Description:"));     form.add(tfAreaDesc);
        form.add(new JLabel("Capacity:"));             form.add(tfCapacity);
        form.add(new JLabel("Temperature Control:"));  form.add(cbTemp);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLoad = new JButton("Load"), btnAdd = new JButton("Add"),
                btnUpdate = new JButton("Update Selected"), btnDelete = new JButton("Delete Selected");
        buttons.add(btnLoad); buttons.add(btnAdd); buttons.add(btnUpdate); buttons.add(btnDelete);

        JPanel south = new JPanel(new BorderLayout());
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        btnLoad.addActionListener(e -> loadLocations());
        btnAdd.addActionListener(e -> addLocation());
        btnUpdate.addActionListener(e -> updateSelectedLocation());
        btnDelete.addActionListener(e -> deleteSelectedLocation());

        loadLocations();
    }

    private void loadLocations(){
        model.setRowCount(0);
        String sql = "SELECT location_id, location_name, area_description, capacity, temperature_control " + "FROM StorageLocation ORDER BY location_name";
        try(Connection c = DBUtils.getConn();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()){
            while(rs.next()){
                model.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getBigDecimal(4),
                        rs.getString(5)
                });
            }
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void addLocation(){
        if(JOptionPane.showConfirmDialog(this,"Add this location?","Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "INSERT INTO StorageLocation (location_name, area_description, capacity, temperature_control) " + "VALUES (?,?,?,?)";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
                ps.setString(1, tfName.getText().trim());
                ps.setString(2, tfAreaDesc.getText().trim());
                ps.setBigDecimal(3, DBUtils.toDecimal(tfCapacity.getText()));
                ps.setString(4, (String) cbTemp.getSelectedItem());
                ps.executeUpdate();
                loadLocations();
                clearForm();
            }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void updateSelectedLocation(){
        int row = table.getSelectedRow();
        if(row < 0){
            DBUtils.info("Select a location first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        if(JOptionPane.showConfirmDialog(this,"Update location #"+id+"?","Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE StorageLocation SET location_name=?, area_description=?, capacity=?, temperature_control=? " + "WHERE location_id=?";
        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement(sql)){
                ps.setString(1, tfName.getText().trim());
                ps.setString(2, tfAreaDesc.getText().trim());
                ps.setBigDecimal(3, DBUtils.toDecimal(tfCapacity.getText()));
                ps.setString(4, (String) cbTemp.getSelectedItem());
                ps.setInt(5, id);
                ps.executeUpdate();
                loadLocations();
        }catch(SQLException ex){
            DBUtils.showErr(ex);
        }
    }

    private void deleteSelectedLocation(){
        int row = table.getSelectedRow();
        if(row < 0){
            DBUtils.info("Select a location first.");
            return;
        }
        int id = (int) model.getValueAt(row, 0);

        if(JOptionPane.showConfirmDialog(this,"Delete location #"+id+"?","Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try(Connection c = DBUtils.getConn();
            PreparedStatement ps = c.prepareStatement("DELETE FROM StorageLocation WHERE location_id=?")){
                ps.setInt(1, id);
                ps.executeUpdate();
                loadLocations();
        }catch(SQLException ex){ 
            DBUtils.showErr(ex);
        }
    }

    private void clearForm(){
        tfName.setText("");
        tfAreaDesc.setText("");
        tfCapacity.setText("0");
        cbTemp.setSelectedIndex(0);
    }
}
