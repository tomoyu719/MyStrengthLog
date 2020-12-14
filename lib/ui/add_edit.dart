import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:uuid/uuid.dart';
import 'package:intl/intl.dart';

import 'package:mystrengthlog/models.dart';
import 'package:mystrengthlog/todo_bloc.dart';

class AddEditScreen extends StatelessWidget {
  String name;
  AddEditScreen([this.name]);

//  TextEditingController _titleController;
//  TextEditingController _descriptionController;
  final _descriptionController = TextEditingController();

  final _formKey = GlobalKey<FormState>();
  var selectedDate = DateTime.now();

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
    return Scaffold(
      appBar: AppBar(
        title: const Text('title'),
      ),
      body: StreamBuilder<Todo>(
        stream: _bloc.todoStream,
        builder: (BuildContext context, AsyncSnapshot<Todo> todo) {
          var syumoku;
          return Column(
            children: <Widget>[
              RaisedButton(
                child: Text('$name'),
                onPressed: () async {
                  syumoku = await Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => ChooseWorkoutScreen(name),
                      fullscreenDialog: true,
                    ));
                },
              ),
              Form(
                key: _formKey,
                child: TextFormField(
                  decoration: const InputDecoration(
//                    icon: Icon(Icons.person),
                    hintText: 'description',
//                    labelText: 'Name *',
                  ),
                  controller: _descriptionController,
                  onSaved: (String v) {
//                    print(v);
                  },
                )
              ),
              RaisedButton(
                child: Text('$selectedDate'),
                onPressed: () async {
                  selectedDate = await showDatePicker(
                    context: context,
                    initialDate: selectedDate,
                    firstDate: DateTime(DateTime.now().year),
                    lastDate: DateTime(DateTime.now().year + 1),
                  );
//                  print(selectedDate.toLocal().toIso8601String());
//                  var formatter = DateFormat('yyyy/MM/dd');
//                  print(formatter.format(selectedDate));
                },
              ),
              RaisedButton(
                child: Text('save'),
                onPressed: (){
                  _bloc.addTodo(
                    Todo(
                      id: Uuid().v4(),
                      date: selectedDate,
//                      date: DateFormat('yyyy/MM/dd').format(selectedDate),
                      title: syumoku,
                      description: _descriptionController.text,
                    )
                  );
                  Navigator.pop(context);
                },
              ),
            ],
          );
        }
      ),
    );
  }
}

class ChooseWorkoutScreen extends StatelessWidget {
  String id;
  ChooseWorkoutScreen([this.id]);
  TextEditingController _titleController;
  TextEditingController _descriptionController;

  final List<String> items = List<String>.generate(20, (i) => 'bp $i');

  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
    _bloc.getTodoById(id);
    return Scaffold(
      appBar: AppBar(
        title: const Text('title'),
      ),
      body: StreamBuilder<Todo>(
        stream: _bloc.todoStream,
        builder: (BuildContext context, AsyncSnapshot<Todo> todo) {
          return GridView.builder(
            itemCount: items.length,
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 3,
            ),
            itemBuilder: (context, index) {
              return _listItem(context, items[index]);
            },
          );
        }
      ),
    );
  }
}

Widget _listItem(BuildContext context, String name) {
  return Card(
    elevation: 8,
    child: InkWell(
      child: Text('$name'),
      onTap: () {
        Navigator.pop<String>(context, name);
      },
    ),
  );
}

