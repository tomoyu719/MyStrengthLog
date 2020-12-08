import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:mystrengthlog/todo_bloc.dart';
import 'package:mystrengthlog/models.dart';
import 'package:mystrengthlog/ui/todo_list.dart';
import 'package:mystrengthlog/ui/add_edit.dart';

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