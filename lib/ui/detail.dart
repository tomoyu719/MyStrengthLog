import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:mystrengthlog/todo_bloc.dart';
import 'package:mystrengthlog/models.dart';
import 'package:mystrengthlog/ui/todo_list.dart';
import 'package:mystrengthlog/ui/add_edit.dart';

class DetailScreen extends StatelessWidget {

  DateTime date;
  DetailScreen({@required this.date});

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
    _bloc.getTodoByDate(date);
//    _bloc.getAllTodos();
    return Scaffold(
      appBar: AppBar(
        title: const Text('detail'),
        actions: <Widget>[
          IconButton(
            icon: Icon(Icons.edit),
            onPressed: (){
              print('edit button pushed');
            },
          )
        ],
      ),
      body: StreamBuilder<List<Todo>>(
          stream: _bloc.todoDateStream,
          builder: (BuildContext context, AsyncSnapshot<List<Todo>> todoList) {
            if (!todoList.hasData) {
              return Center(child:CircularProgressIndicator());
            } else {
              return ListView.builder(
                itemCount: todoList.data.length,
                itemBuilder: (BuildContext context, int index) {
                  Todo todo = todoList.data[index];
                  return Dismissible(
                    key: Key(todo.id),
                    onDismissed: (direction) {

                    },
//                    background: Container(color: Colors.pinkAccent),
                    child: ListTile(
                      title: Text(todo.title),
                      subtitle: Text(todo.description),
                    )
                  );
                }
              );
            }
          }
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          print('fab pushed');
//          Navigator.pop(context);
//          Navigator.push(
//              context,
//              MaterialPageRoute(
//                builder: (context) => AddEditScreen(id),
//                fullscreenDialog: true,
//              ));
        },
        child: const Icon(Icons.edit),
      ),
    );
  }
}