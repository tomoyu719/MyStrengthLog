import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';

import 'package:mystrengthlog/models.dart';
import 'package:mystrengthlog/todo_bloc.dart';
import 'package:mystrengthlog/ui/add_edit.dart';
import 'package:mystrengthlog/ui/detail.dart';

class ListScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    final _bloc = Provider.of<TodoBloc>(context, listen: false);
//    print(_bloc);
    return Scaffold(
      appBar: AppBar(
          title: Text('list'),
          actions: <Widget>[
            IconButton(
              icon: Icon(Icons.filter_list),
              onPressed: () async {
                final name = await Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => ChooseWorkoutScreen(),
                      fullscreenDialog: true,
                    )
                );
                _bloc.getFilterdList(name);
              },
            ),
            IconButton(
              icon: Icon(Icons.calendar_today),
              onPressed: () async {
                final date = await showDatePicker(
                  context: context,
                  initialDate: DateTime.now(),
                  firstDate: DateTime(DateTime.now().year),
                  lastDate: DateTime(DateTime.now().year + 1),
                );
                _bloc.getTodoByDate(date);
              },
            ),
          ],
      ),
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
//                    _bloc.deleteTodo(todo.id);
                    Scaffold.of(context)
                        .showSnackBar(SnackBar(content: Text("${todo.id} dismissed")));
                  },
                  background: Container(color: Colors.pinkAccent),
                  child: ListTile(
                    leading: Text('${DateFormat('MM/dd').format(todo.date)}'),
                    title: Text("${todo.title}"),
//                    subtitle: Text("${todo.date}\n" + "${todo.description}"),
                    onTap: () {
                      Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (context) => DetailScreen(date: todo.date),
                            fullscreenDialog: true,
                          ));
                    },
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