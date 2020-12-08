import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:uuid/uuid.dart';

import 'package:mystrengthlog/models.dart';
import 'package:mystrengthlog/todo_bloc.dart';

void main() {
  runApp(MyApp());
}

//class MyApp extends StatelessWidget {
//
//  @override
//  Widget build(BuildContext context) {
//    return MaterialApp(
//      title: 'Flutter Demo',
//      theme: ThemeData(
//        primarySwatch: Colors.blue,
////        visualDensity: VisualDensity.adaptivePlatformDensity,
//      ),
////      home: MyHomePage(title: 'Flutter Demo Home Page'),
//      home: MyHomePage(),
//    );
//  }
//}

//class MyHomePage extends StatelessWidget {
class MyApp extends StatelessWidget {

  @override
  Widget build(BuildContext context) {

    return MultiProvider(
      providers: [
        Provider<TodoBloc>(
          create: (context) => TodoBloc(),
          dispose: (_, bloc) => bloc.dispose(),
        ),
      ],
      child: MaterialApp(
        title: 'demo',
        theme: ThemeData(
          primarySwatch: Colors.blue,
          visualDensity: VisualDensity.adaptivePlatformDensity,
        ),
        home: ListScreen(),
      ),
    );
  }
}

class ListScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
    return Scaffold(
      appBar: AppBar(title: Text('list')),
      body: StreamBuilder<List<Todo>>(
          stream: _bloc.todoListStream,
          builder: (BuildContext context, AsyncSnapshot<List<Todo>> todoList) {
            if (!todoList.hasData) {
              return Center(child:CircularProgressIndicator());
            }
            else {
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
          }
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          print('pushed');
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
  AddEditScreen([this.id]);
  TextEditingController _titleController;
  TextEditingController _descriptionController;

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
          if (!todo.hasData && id != null) {
            return Center(child:CircularProgressIndicator());
          }
          else{
            return Form(
              key: _formKey,
              child: Column(
                children: [
                  TextFormField(
//                    initialValue: todo.data.title + "",
                  ),
                  TextFormField(
//                    initialValue: todo?.data.description + "",
                  ),
                  RaisedButton(
                    onPressed: () {
                      print(todo);
                    },
                  ),
                ]
              ),
            );
          }
        }
      ),
    );
  }
}

class DetailScreen extends StatelessWidget {

  String id;
  DetailScreen({@required this.id});

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
    _bloc.getTodoById(id);
    return Scaffold(
      appBar: AppBar(
        title: const Text('detail'),
      ),
      body: StreamBuilder<Todo>(
        stream: _bloc.todoStream,
        builder: (BuildContext context, AsyncSnapshot<Todo> todo) {
          if (!todo.hasData) {
            return Center(child:CircularProgressIndicator());
          } else {
            return Column(
              children: <Widget>[
                Text(todo.data.title),
                Text("${todo.data.date}"),
                Text(todo.data.description),
              ]
            );
          }
        }
      ),


      floatingActionButton: FloatingActionButton(
        onPressed: () {
//          print('pushed' * 10);
          Navigator.pop(context);
          Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => AddEditScreen(id),
                fullscreenDialog: true,
              ));
//          print('pushed2' * 10);

        },
        child: const Icon(Icons.edit),
      ),
    );
  }
}