import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'package:mystrengthlog/models.dart';
import 'package:mystrengthlog/todo_bloc.dart';
import 'package:mystrengthlog/ui/add_edit.dart';
import 'package:mystrengthlog/ui/detail.dart';

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