import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:uuid/uuid.dart';

import 'package:mystrengthlog/models.dart';
import 'package:mystrengthlog/todo_bloc.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
//        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
//      home: MyHomePage(title: 'Flutter Demo Home Page'),
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatelessWidget {

  @override
  Widget build(BuildContext context) {

    return Provider<TodoBloc>(
      create: (context) => TodoBloc(),
      dispose: (_, bloc) => bloc.dispose(),
      child: ListScreen(),
    );

  }
}

class ListScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
//    _bloc.addTodo();
    return Scaffold(
      appBar: AppBar(title: Text('list')),
      body: StreamBuilder<List<Todo>>(
        stream: _bloc.todoListStream,
        builder: (BuildContext context, AsyncSnapshot<List<Todo>> todoList) {
          if (todoList.hasData) {
            return ListView.builder(
              itemCount: todoList.data.length,
              itemBuilder: (BuildContext context, int index) {
                Todo todo = todoList.data[index];
                return Dismissible(
                  key: Key(todo.id),
                  onDismissed: (direction) {
                    _bloc.deleteTodo(todo.id);
                  },
                  child: ListTile(
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => DetailScreen(id: todo.id),
                          fullscreenDialog: true,
                        )
                      );
                    },
                    title: Text("${todo.title}"),
                    subtitle: Text("${todo.date}\n" + "${todo.description}"),
                  )
                );
              },
            );
          }
          return Center(child:CircularProgressIndicator());
        }
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => AddEditScreen(),
              fullscreenDialog: true,
            ));
          },
        child: Icon(Icons.add),
      ),
    );
  }
}

class AddEditScreen extends StatelessWidget {
  String id;
//  Todo todo;
//  String id ?? Uuid().v4();
//  AddEditScreen([this.id = Todo('Uuid().v4()', DateTime.now(), '', '')]);

  AddEditScreen({this.id});
  TextEditingController _titleController;
  TextEditingController _descriptionController;

  final _formKey = GlobalKey<FormState>();

//  final todo = Provider.of<TodoBloc>(context, listen: false).getTodoById(id);

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
            return Form(
              key: _formKey,
              child: Column(
                children: [
                  TextFormField(
                    initialValue: todo.data.title ?? '',
                    controller: _titleController,
                    decoration: InputDecoration(
                      hintText: 'title',
                    ),
                  ),
                  TextFormField(
                    initialValue: todo.data.description ?? '',
                    controller: _descriptionController,
                    decoration: InputDecoration(
                      hintText: 'description',
                    ),
                  ),
                  RaisedButton(
                    onPressed: () async {
                      final selectedDate = await showDatePicker(
                        context: context,
                        initialDate: todo.data.date ?? '',
                        firstDate: DateTime(DateTime.now().year),
                        lastDate: DateTime(DateTime.now().year + 1),
                      );
                      print(selectedDate);
                    },
                  ),
                  RaisedButton(
                    onPressed: (){
//                TodoBloc().addTodo();
//                Navigator.pop(context);
                    },
                  ),
                ],
              ),
            )
          }
      ),
    );
  }
}

class DetailScreen extends StatelessWidget {

//  Todo todo;
  String id;
  DetailScreen({@required this.id});

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
    return Scaffold(
        appBar: AppBar(
          title: const Text('detail'),
        ),
        body: Column(
          children: <Widget>[
            Text(todo.title),
            Text("${todo.date}"),
            Text(todo.description),
          ]

        ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.pop(context);
          Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => AddEditScreen(todo),
                fullscreenDialog: true,
              ));
        },
        child: const Icon(Icons.edit),
      ),
    );
  }
}